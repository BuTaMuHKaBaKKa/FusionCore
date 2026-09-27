@echo off
setlocal
cd /d "%~dp0"
set "FUSIONCORE_JAR=fusioncore-neoforge-1.21.1-0.2.1.jar"
set "FUSIONCORE_EXIT=1"
if not defined FUSIONCORE_MEMORY set "FUSIONCORE_MEMORY=4G"
set "FUSIONCORE_JAVA_FOUND="
if defined FUSIONCORE_JAVA call :try_java "%FUSIONCORE_JAVA%"
call :try_java "%~dp0runtime\bin\java.exe"
if defined JAVA21_HOME call :try_java "%JAVA21_HOME%\bin\java.exe"
if defined JAVA_HOME call :try_java "%JAVA_HOME%\bin\java.exe"
for /d %%J in ("%ProgramFiles%\Eclipse Adoptium\jdk-21*" "%ProgramFiles%\Java\jdk-21*") do call :try_java "%%~fJ\bin\java.exe"
for /f "delims=" %%J in ('where.exe java.exe 2^>nul') do call :try_java "%%J"
if not defined FUSIONCORE_JAVA_FOUND (
    echo Java 21 was not found. Install 64-bit Java 21 or set JAVA21_HOME.
    echo Download: https://adoptium.net/temurin/releases/?version=21
    goto finish
)
if not exist "%FUSIONCORE_JAR%" (
    echo Missing %FUSIONCORE_JAR% next to start.bat.
    goto finish
)
echo Starting Fusion Core 0.2.1 - Minecraft 1.21.1 / NeoForge 21.1.250
echo Java: "%FUSIONCORE_JAVA_FOUND%"
echo Maximum Java heap: %FUSIONCORE_MEMORY%
"%FUSIONCORE_JAVA_FOUND%" "-Xmx%FUSIONCORE_MEMORY%" -Dfile.encoding=UTF-8 %FUSIONCORE_JAVA_OPTS% -jar "%FUSIONCORE_JAR%" nogui %*
set "FUSIONCORE_EXIT=%ERRORLEVEL%"
if not "%FUSIONCORE_EXIT%"=="0" echo Fusion Core exited with code %FUSIONCORE_EXIT%. Read logs\latest.log.
:finish
if "%FUSIONCORE_NO_PAUSE%"=="1" exit /b %FUSIONCORE_EXIT%
pause
exit /b %FUSIONCORE_EXIT%

:try_java
if defined FUSIONCORE_JAVA_FOUND exit /b 0
if not exist "%~1" exit /b 0
"%~1" -XshowSettings:properties -version 2>&1 | "%SystemRoot%\System32\findstr.exe" /R /C:"java.specification.version = 21$" >nul
if not errorlevel 1 set "FUSIONCORE_JAVA_FOUND=%~1"
exit /b 0
