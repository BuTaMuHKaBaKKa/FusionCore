<#
.SYNOPSIS
Builds Fusion Core from source on Windows.

.DESCRIPTION
Wraps the Gradle build with the two settings this project needs on Windows:

  * JDK 21, which Architectury Loom and Minecraft 1.21.1 require.
  * -Djava.net.useSystemProxies=true in JAVA_TOOL_OPTIONS. Gradle forks child JVMs
    (notably Spigot BuildTools), and those inherit JAVA_TOOL_OPTIONS but not the
    HTTP_PROXY/HTTPS_PROXY environment variables. On a network that only reaches
    the internet through a proxy, BuildTools' git clone of hub.spigotmc.org stalls
    silently without this.

The first build runs Spigot BuildTools, which clones and compiles CraftBukkit.
Expect it to take a long time and several GB of disk.

.PARAMETER JavaHome
Path to a JDK 21. Defaults to $env:JAVA_HOME.

.PARAMETER AllPlatforms
Build the Forge and Fabric jars as well, not just NeoForge.

.PARAMETER UseDownloadRelay
Route Maven downloads through a local relay backed by curl.exe. Use only if
Gradle's own HTTPS downloads stall on this machine while curl works. Needs Node.js.

.EXAMPLE
.\build-fusioncore.ps1 -JavaHome 'C:\Program Files\Java\jdk-21'
#>
param(
    [string]$JavaHome = $env:JAVA_HOME,
    [switch]$AllPlatforms,
    [switch]$UseDownloadRelay
)
$ErrorActionPreference = 'Stop'

if (-not $JavaHome -or -not (Test-Path -LiteralPath (Join-Path $JavaHome 'bin/javac.exe'))) {
    throw 'Set JAVA_HOME to a JDK 21, or pass -JavaHome <path-to-jdk-21>.'
}
$fusionJava = Join-Path $JavaHome 'bin/java.exe'
$fusionVersion = (& $fusionJava -version 2>&1 | Out-String)
if ($fusionVersion -notmatch 'version "21\.') {
    throw "Building Fusion Core for Minecraft 1.21.1 requires JDK 21. Found:`n$fusionVersion"
}

$fusionOldJava = $env:JAVA_HOME
$fusionOldPath = $env:PATH
$fusionOldOptions = $env:JAVA_TOOL_OPTIONS
$fusionOldHttpsProxy = $env:HTTPS_PROXY
$fusionRelay = $null

Push-Location $PSScriptRoot
try {
    $env:JAVA_HOME = (Resolve-Path -LiteralPath $JavaHome).Path
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
    $env:JAVA_TOOL_OPTIONS = "$fusionOldOptions -Djava.net.useSystemProxies=true".Trim()

    if ($AllPlatforms) {
        $fusionBuildArgs = @('build', 'collect', '--no-daemon')
    } else {
        $fusionBuildArgs = @(':bootstrap:neoforgeJar', '--no-daemon')
    }

    if ($UseDownloadRelay) {
        $fusionNode = (Get-Command node -ErrorAction Stop).Source
        Get-Command curl.exe -ErrorAction Stop | Out-Null
        New-Item -ItemType Directory -Force -Path (Join-Path $PSScriptRoot 'build') | Out-Null
        $fusionRelayLog = Join-Path $PSScriptRoot 'build/relay.log'
        $fusionRelayScript = Join-Path $PSScriptRoot 'build-support/maven-relay.cjs'
        $fusionProxy = [Net.WebRequest]::GetSystemWebProxy().GetProxy([Uri]'https://maven.izzel.io/releases/')
        if ($fusionProxy.Host -ne 'maven.izzel.io') { $env:HTTPS_PROXY = $fusionProxy.AbsoluteUri }
        $fusionRelay = Start-Process -FilePath $fusionNode -ArgumentList @('"' + $fusionRelayScript + '"') -WindowStyle Hidden -PassThru -RedirectStandardOutput $fusionRelayLog -RedirectStandardError (Join-Path $PSScriptRoot 'build/relay-error.log')
        Start-Sleep -Seconds 2
        if ($fusionRelay.HasExited) { throw 'Download relay failed to start. Check build/relay-error.log and port 18761.' }
        $fusionBuildArgs += @('--init-script', 'build-support/local-repositories.gradle', '-Dorg.gradle.internal.http.socketTimeout=180000')
    }

    & .\gradlew.bat @fusionBuildArgs
    if ($LASTEXITCODE -ne 0) { throw 'Fusion Core build failed. See the Gradle output.' }

    Get-ChildItem -Path (Join-Path $PSScriptRoot 'bootstrap/build/libs') -Filter 'fusioncore-*.jar' -ErrorAction SilentlyContinue |
        ForEach-Object { Write-Output ("Built: " + $_.FullName) }
} finally {
    if ($fusionRelay -and -not $fusionRelay.HasExited) { $fusionRelay.Kill() }
    $env:JAVA_HOME = $fusionOldJava
    $env:PATH = $fusionOldPath
    $env:JAVA_TOOL_OPTIONS = $fusionOldOptions
    $env:HTTPS_PROXY = $fusionOldHttpsProxy
    Pop-Location
}
