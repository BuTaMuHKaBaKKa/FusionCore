# Fusion Core 0.2.1

> **Beta.** Fusion Core is being tested before its first stable release. Back up your worlds, and please report problems
> through [Issues](https://github.com/BuTaMuHKaBaKKa/FusionCore/issues) with the full `logs/latest.log`.

A Bukkit/Spigot server that also loads NeoForge mods, for **Minecraft 1.21.1** on
**NeoForge 21.1.250**, running on **Java 21**.

Fusion Core is a fork of [Arclight](https://github.com/IzzelAliz/Arclight) (branch
`FeudalKings`, commit `d27101f1fd155528ca29baf41b70f91beb3e0187`). It is a derivative
project, not a clean-room implementation. See [FUSIONCORE-CHANGES.md](FUSIONCORE-CHANGES.md)
for what differs from upstream, [README-UPSTREAM.md](README-UPSTREAM.md) for the original
project description and [LICENSE](LICENSE) for the GPL-3.0 terms.

Forge and Fabric jars are still built from the same tree; NeoForge is the platform
Fusion Core targets.

Fusion Core was called VitaCore up to 0.2.0; 0.2.1 is the first release under the new name. A server that ran a VitaCore build keeps its settings and data:
`vitacore.conf` becomes `fusioncore.conf` and `.vitacore/` becomes `.fusioncore/` on the first start
(the same happens to Arclight's `arclight.conf` and `.arclight/`), and owners recorded for mods' machines
are read under the old key.

## Running a server

See [distribution/README-RU.md](distribution/README-RU.md) (Russian) for the end-user
setup guide. In short: put `fusioncore-neoforge-1.21.1-0.2.1.jar` in an empty folder
together with `start.bat` or `start.sh`, accept the Minecraft EULA in `eula.txt` if you
agree to it, and run the launcher. NeoForge, Minecraft and the required libraries are
downloaded on first start.

Bukkit/Spigot plugins for Minecraft 1.21 go in `plugins/`, NeoForge mods for 1.21.1 in
`mods/`.

Fusion Core adds `/fusioncore`, guarded by the operator-default permission `fusioncore.admin`:

| Command | Shows |
|---|---|
| `/fusioncore` | build and loader version |
| `/fusioncore status` | mod/plugin/world/player counts, heap use, JVM uptime |
| `/fusioncore mods` | loaded mods |
| `/fusioncore plugins` | loaded plugins and whether each is enabled |

### Plugins: Bukkit, Spigot and Paper

Besides Bukkit and Spigot, Fusion Core carries the **Paper 1.21.1 API** (Paper build 132, commit
`b48403bd`): Adventure components, Paper's events fired from the core, `paper-plugin.yml`
plugins with bootstrappers and loaders, Brigadier commands and lifecycle events, registry
access and modification. A member-by-member check of paper-api 1.21.1 against the core finds
2 of 26274 members missing, both on purpose. Plugins built with Mojang's names
(`paperweight-mappings-namespace: mojang`) load as they do on Paper. Purpur's own API is not
provided. Paper's server internals (classes outside its API that some plugins reach into) are
only partly imitated, so a plugin that depends on them may still fail; see
[FUSIONCORE-CHANGES.md](FUSIONCORE-CHANGES.md).

A test run of 45 widely used plugins (ViaVersion, Geyser, WorldGuard with FastAsyncWorldEdit,
Essentials, MythicMobs, Skript, dynmap, squaremap, GriefPrevention and others) starts them
all except where the plugin itself is the cause (a missing dependency, a version mismatch
between plugins, a jar built for a newer Java, a download that needs network access) and
ImageFrame, whose bundled class scanner cannot list NeoForge's module files.

### Mods' machines and protection plugins

Protection plugins only see Bukkit events. A mod's machine that breaks or places blocks with no
player (Create's drill, saw, harvester, contraptions) used to fire none, so it reached into
claims. Fusion Core records who placed a mod's machine and fires `BlockBreakEvent`/`BlockPlaceEvent`
for the machine's changes as that player: a drill works inside its owner's claim and not in
someone else's. Settings are in the `machines` section of `fusioncore.conf`. Machines placed
before this feature have no owner and work as before unless `unknown-owner` says otherwise.
Login plugins such as AuthMe block a machine while its owner is not logged in.

### World names

A new server names the vanilla nether and end `world_nether` and `world_the_end`, as Paper
does (`compatibility.symlink-world` in `fusioncore.conf`). A server that already had a world
when it first started Fusion Core keeps Arclight's names (`world/DIM-1`, `world/DIM1`), because
plugin data are stored under them.

### Behind Velocity

The shipped `server.properties` is for a standalone server. Behind a Velocity proxy with
modern forwarding, set `online-mode=false`, set `velocity { enable = true, secret = "..." }`
in `fusioncore.conf` to the proxy's `forwarding.secret`, and firewall the backend port so that
only the proxy can reach it (`server-ip=127.0.0.1` if both run on one host). Never run
`online-mode=false` without `velocity.enable = true`: nothing then checks who is logging in. Details in
[distribution/README-RU.md](distribution/README-RU.md).

## Building from source

Requirements: **JDK 21**, Git, internet access, and several GB of free disk and memory.
The Gradle wrapper downloads Gradle 8.13.

The first build runs Spigot BuildTools, which clones Bukkit, CraftBukkit, Spigot and
BuildData and then decompiles and patches CraftBukkit. This takes a long time; later
builds reuse the result from `.gradle/arclight/`.

Windows:

```powershell
.\build-fusioncore.ps1 -JavaHome 'C:\Path\To\jdk-21'
```

Or Gradle directly:

```bash
./gradlew :bootstrap:neoforgeJar --no-daemon
```

Add `-AllPlatforms` to the script, or run `./gradlew build collect --no-daemon`, to build
the Forge and Fabric jars too. Output lands in `bootstrap/build/libs/` (`collect` copies all
three to `build/libs/`). The Forge and Fabric jars are built but not tested; NeoForge is the
supported platform. The GitHub Actions workflow runs the same build on every push.

### Pinned versions

`fabric-loader` and `fabric-api` in `gradle/libs.versions.toml` are held below what
upstream carries, because newer fabric-loader drags in a sponge-mixin whose `@Redirect`
declaration makes every compiled mixin unreadable to NeoForge's own mixin at runtime.
The server then dies during mixin apply with a `ClassCastException` and no message in
`logs/latest.log`. See FUSIONCORE-CHANGES.md before bumping them.

### If downloads stall

If your internet access goes through a proxy, export
`JAVA_TOOL_OPTIONS=-Djava.net.useSystemProxies=true` before building. Gradle forks child
JVMs — BuildTools among them — and those inherit `JAVA_TOOL_OPTIONS` but not
`HTTP_PROXY`/`HTTPS_PROXY`. Without it BuildTools' clone of `hub.spigotmc.org` stalls
with no error message. `build-fusioncore.ps1` sets this for you.

If Gradle's own HTTPS downloads stall on Windows while `curl.exe` works, run
`build-fusioncore.ps1 -JavaHome 'C:\Path\To\jdk-21' -UseDownloadRelay`. That needs Node.js;
it starts a loopback relay on `127.0.0.1:18761`, caches artifacts in
`build/maven-relay-cache` and stops the relay afterwards. Certificate validation for the
upstream repositories is done by curl; system network settings are not changed.

## Repository layout

| Path | Contents |
|---|---|
| `fusioncore-common` | platform-agnostic Bukkit layer and mixins |
| `fusioncore-neoforge` / `fusioncore-forge` / `fusioncore-fabric` | per-loader code |
| `bootstrap` | launcher, class transformers, jar assembly |
| `installer` | downloads Minecraft and the loader at first start |
| `i18n-config` | configuration and translations |
| `distribution` | server package files: launchers, `server.properties`, notices |
| `tests` | integration probe plugin and mod; not part of a server package |
| `build-support` | optional local download relay for constrained networks |
