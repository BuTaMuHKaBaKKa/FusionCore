# Fusion Core — changes from Arclight

The project was called VitaCore up to 0.2.0 and was renamed to Fusion Core in 0.2.1, its first public release. Every name a server
sees changed with it: the jar (`fusioncore-neoforge-1.21.1-<version>.jar`), `fusioncore.conf`, the `.fusioncore/` working
folder, the `/fusioncore` command and the `fusioncore.admin` permission, the brand `fusioncore:fusioncore`. Files left by a
VitaCore or Arclight build (`vitacore.conf`/`arclight.conf`, `.vitacore/`/`.arclight/`) are renamed on the first start, and
machine owners stored under `vitacore:owner` are still read. The Java packages (`io.izzel.arclight.*`) and the mod id
`arclight`, which mods use to detect the hybrid, are Arclight's and stay as they are.

## Fusion Core 0.2.0 — 2026-09-20

Port of Fusion Core from Minecraft 1.20.1 / Forge to **Minecraft 1.21.1 / NeoForge 21.1.250**.

Base: [Arclight](https://github.com/IzzelAliz/Arclight), branch `FeudalKings`, commit
`d27101f1fd155528ca29baf41b70f91beb3e0187`. The previous release (0.1.2) was based on
Arclight Trials/1.0.6, commit `4e94eb7677e6ae86c838c1616151456ad6293ab9`.

This is not an in-place upgrade of a 0.1.2 server. Both the game version and the mod
loader changed; mods and plugins must be replaced with 1.21.1 builds, and a 1.20.1 world
is converted irreversibly on first launch.

## What changed in the base project

Upstream rebuilt its toolchain between the two commits, so most of Fusion Core's 0.1.2 build
changes had nothing left to apply to:

| | 0.1.2 base | 0.2.0 base |
|---|---|---|
| Build | ForgeGradle 5.1.77 + MixinGradle | Architectury Loom 1.9, `gradle/libs.versions.toml` |
| Java | 17 | 21 |
| Modules | common, arclight-forge, forge-installer, i18n-config | common, forge/neoforge/fabric, bootstrap, installer, i18n-config |
| Loader | Forge 47.4.10 | NeoForge 21.1.250 (Forge and Fabric also built) |
| Bukkit | v1_20_R1 | v1_21_R1, Spigot reversion 4344 |

## Carried over

### Branding and identity

- `CraftServer` name and server brand report `Fusion Core`; `getServerModName()` is prefixed.
- Mod metadata (`neoforge.mods.toml`, `mods.toml`, `fabric.mod.json`) names Fusion Core and
  credits both Fusion Core contributors and upstream Arclight.
- Jar manifests carry `Implementation-Title: Fusion Core`, a `fusioncore-<platform>-<mc>-<version>`
  implementation version, a Fusion Core vendor string and a `FusionCore-Upstream` attribute
  recording the exact upstream commit.
- Artifacts are named `fusioncore-neoforge-1.21.1-0.2.0.jar` (likewise for forge and fabric).
- The boot banner is a plain branded line instead of the Arclight ASCII logo.
- The Java-version check in the launcher reports Fusion Core.

### Features

- `/fusioncore` diagnostic command with `fusioncore.admin` (operator default) and
  `info` / `status` / `mods` / `plugins` subcommands, registered at server creation and
  again after a Bukkit command-map reload.
- `/version` describes the build instead of comparing it against Spigot's commit history,
  where Fusion Core builds do not appear.

### Compatibility fixes still absent upstream

- `ThrownPotion` uses vanilla's source-aware `addEffect` overload, so mods that key their
  handling off the effect source (for example Passive Skill Tree) keep working.
- `RecipeManager.removeRecipe` copies immutable tables before removing, the way
  `addRecipe` already does, so a mod that installs an immutable snapshot after `apply()`
  does not break Bukkit recipe removal.
- `Entity.getIndirectPassengers` honours Minecraft's `Iterable` contract instead of
  casting another mod's result to `Collection`.
- Bootstrap extraction (Forge and NeoForge) compares the embedded jar with the extracted one
  in memory and only writes a differing build, through a temporary file and a move, so
  rebuilding the same version actually loads the new code on restart. An unchanged build
  writes nothing, and a failed temporary-file cleanup or a replace blocked by an open handle
  is logged instead of aborting startup. Fabric keeps upstream's extraction in
  `ArclightGameProvider.extract()`: it only extracts when the versioned jar is missing.
- The installer prefers official Mojang, Maven Central and NeoForged endpoints over
  third-party mirrors.
- A connection that fails in the handshake or status phase (a server list ping the client
  drops early) is closed without a disconnect packet. Vanilla tries to send the play
  disconnect packet, which those phases do not have, and logs "Error sending packet
  clientbound/minecraft:disconnect". Port of Paper's patch for the same bug
  (`ConnectionMixin`).
- The Windows console prints Unicode on NeoForge. NeoForge ships jline-terminal 3.20.0,
  which rejects upstream's jline-terminal-jansi 3.12.1 provider, so the console fell back to
  a dumb terminal writing in the console code page and showed `?` for anything outside it.
  `installer.json` now has a NeoForge-only list (`neoforgeExtra`, from
  `installerLibsNeoforge` in `bootstrap/build.gradle`) with the matching
  jline-terminal-jna 3.20.0; `InstallInfo#neoforgeDeps()` drops the Jansi provider. Forge
  and Fabric keep the old libraries.
- The NeoForge `arclight-log4j2.xml` no longer sets `packages=`. Log4j 2.22 printed a
  deprecation warning per listed package on every configuration load; the plugins the config
  uses are all indexed in their jars' `Log4j2Plugins.dat`, as NeoForge's own config relies on.
- The NeoForge mod version is plain `0.2.0`, without the git hash. NeoForge keeps mod versions
  in `level.dat` and warned "mods have version differences" after every build. The hash is
  still in the jar manifest, the startup banner and `/version`.
- Routine mod messages can be kept out of the console and `latest.log` while staying in
  `debug.log`: the `Async` appender refs in the NeoForge `arclight-log4j2.xml` carry a
  `RegexFilter`. Currently only Sable's "Saving sub-levels for level ..." (three lines per
  autosave).
- Legacy strings with `§x` RGB codes sent to the console (hand-built gradients, e.g.
  UltimateAutoRestart's prefix) are rewritten with the nearest named colours before logging
  (`FusionCoreAdventure#forConsole`). `%minecraftFormatting` does not know `§x` and printed
  `§xU§xl…`; Paper shows these strings in colour.
- With Alternate Current installed, redstone wire fires Bukkit's `BlockRedstoneEvent` again.
  Alternate Current cancels `RedStoneWireBlock#updatePowerStrength`, where the event is fired, and
  sets wire power itself. `compat.alternatecurrent.WireNodeMixin` (NeoForge, only loaded with the
  mod) fires the event where Paper's own port of Alternate Current does, in `WireNode#setPower`
  before the new state is written; a listener's new current is clamped to 0–15 (Paper would throw).
- NeoForge logs every exception on a configuration or play connection as ERROR with a stack trace
  ("Exception caught in connection"); vanilla and Paper do not log it. `ConnectionMixin_NeoForge`
  logs `IOException`s (a client gone without a disconnect: "Connection reset") at DEBUG, which
  `net.minecraft` loggers do not record, like vanilla's own debug lines there; other exceptions stay
  ERROR.
- The connection throttle (`connection-throttle` in `bukkit.yml`) exempts every loopback address,
  `127.0.0.0/8` and `::1`. CraftBukkit compares the address with the string `"127.0.0.1"`, so a client
  on the same machine that connects over IPv6 (`::1`, Windows' default for `localhost`) was throttled
  like a remote one. Other addresses are throttled as before.
- Arclight's longer keep-alive timeout (at least 25 s instead of 15 s) is an `@ModifyExpressionValue`
  instead of an `@ModifyConstant`. With a mod that also changes that constant (Connectivity), Mixin
  skipped Arclight's injector with a "@ModifyConstant conflict" WARN; expression wrappers chain, and
  `max(value, 25 s)` keeps a longer timeout set by the mod.
- Vanilla warns "Assets URL '...' uses unexpected schema" when the game jar's assets root is not a
  `jar:`/`file:` URL. On NeoForge it is mounted through the `union:` file system, so this printed twice
  on every start although the path resolves; `union:` is now accepted (`VanillaPackResourcesBuilderMixin`),
  other schemes still warn.
- The integrated WorldEdit patcher logs "Loading WorldEdit Bukkit support ..." at INFO and no
  longer adds a WARN asking users to report WorldEdit issues to upstream Arclight.

### Radium/Lithium

- `mixin.ai.poi.fast_portals` is disabled through the override mechanism Lithium
  documents, declared in the loader metadata for all three platforms. Lithium overwrites
  `PortalForcer.findClosestPortalPosition`, where Arclight applies the Bukkit portal search
  radius. (0.1.2 also disabled `mixin.world.player_chunk_tick`; Lithium 1.21.1 has no such
  option and warned about it on every start, so it was dropped.) The modpack's own configuration files are not touched. Inert when no
  Lithium-family mod is installed.
- Arclight's optional entity-list and tracked-entity optimizations yield to an installed
  Lithium-family mod.

## Reworked for NeoForge

- **`FusionCoreOptimizationPlugin` is gone.** It was an `IMixinConfigPlugin` reading
  `net.minecraftforge.fml.loading.LoadingModList`, declared as the `"plugin"` of
  `mixins.arclight.impl.forge.optimization.json`. Neither part survives: a mixin config
  may only carry one plugin and upstream's `ArclightMixinPlugin` already owns it, and the
  config was renamed. Upstream now provides a declarative equivalent, so the same effect
  is expressed as `@LoadIfMod(modid = {LITHIUM, CANARY, RADIUM}, condition = ABSENT)` on
  `EntityMixin_Optimize` and `ChunkMapMixin_Optimize$TrackedEntityMixin`. It resolves mods
  through the platform abstraction, so it works on NeoForge, Forge and Fabric alike.
  `ClassInstanceMultiMapMixin`, the third mixin Fusion Core used to skip, already carries
  that annotation upstream. One behaviour is lost: the plugin logged each skipped mixin,
  and the upstream mechanism is silent.
- **`FusionCoreCommand` no longer imports FML.** It used `net.minecraftforge.fml.ModList`
  and `ForgeVersion`, which cannot live in the platform-agnostic module. `ArclightCommon.Api`
  gained `loaderName()`, `loaderVersion()` and `loadedMods()`, implemented for NeoForge,
  Forge and Fabric; the command reads those. Version and Minecraft version are read from
  the jar manifest and `Bukkit.getBukkitVersion()` rather than hardcoded.
- **`NetworkHooksMixin` is gone.** NeoForge 21.1 removed `NetworkHooks`. Upstream's Forge
  counterpart is `IForgeServerPlayerMixin`; NeoForge has no equivalent because `openMenu`
  is served by the common `ServerPlayerMixin`. The fix this file carried is obsolete
  anyway — see below.
- **`forge-installer` became `installer`**, so the mirror ordering moved with it.

## Dropped, because upstream fixed it first

- **Nested container-owner restore.** 0.1.2 saved and restored the previous owner around
  `callInventoryOpenEvent` in `ServerPlayerMixin` and `NetworkHooksMixin`. Upstream now
  keeps `ArclightCaptures.containerOwner` as a `Stack<Player>` with a `popContainerOwner`,
  and every call site is already wrapped in try/finally. That is a strictly better fix.
- **Mutable recipe maps.** 0.1.2 replaced the `apply` overwrite with a return injection.
  Upstream independently did the same (`arclight$makeMutable`, plus `replaceRecipes`),
  on the 1.21 `RecipeHolder`/`Multimap` structures. The `addRecipe` null-table guard is
  moot for the same reason.

## Deliberately not carried over

- **The `ThrownEgg` rework.** 0.1.2 split Bukkit event state into helpers so the vanilla
  local-variable layout survived for Vanilla Backport's chicken variants. That mod's role
  was specific to the 1.20.1 modpack, the 1.21.1 method gained
  `fudgePositionAfterSizeChange` and an entity-removal cause, and the target modpack for
  1.21.1 is not decided. Upstream's version is used. Revisit if Vanilla Backport (or a
  mod injecting into `ThrownEgg.onHit`) ends up in the mod set.
- **Removing the `openMenu` close redirect.** 0.1.2 deleted `arclight$skipSwitch` to keep
  the loader's own menu-close path. Upstream kept it and rewrote it on `@Decorate` /
  `DecorationOps`, and now maintains its own Fabric module, so the original rationale is
  upstream's concern. Removing it here would need separate verification that
  `InventoryCloseEvent` is not duplicated and the client does not desync on a menu switch.
- **The CI workflow.** Not ported.

## Paper compatibility layer

Arclight is built on Spigot, so a plugin compiled against Paper or Purpur fails at
runtime on API Spigot does not have. Fusion Core bridges the cheap parts of that gap.
This is a compatibility shim, not Paper support: the server is still Spigot underneath.

Adventure is shipped as a runtime library (the installer fetches
`net.kyori:adventure-*` 4.17.0 alongside the other libraries), and
`CommandSenderMixin` adds `Audience` to `CommandSender`. In Adventure 4.17 every
`sendMessage` overload ends in `sendMessage(Identity, Component, MessageType)`, which is the
method bridged:

- **Players** (`CraftPlayerAudienceMixin`) get the component as a vanilla component, so RGB,
  click, hover and insertion survive. Action bar, titles (`sendTitlePart`, `clearTitle`) and
  `playSound`/`stopSound` work too.
- **Everyone else** goes through Bukkit's legacy `String` path with named colours only: the
  console's TerminalConsoleAppender does not understand `§x`.
- Boss bars, books, tab-list header/footer keep Adventure's default no-op.

Components cross between Adventure and vanilla as JSON (`FusionCoreAdventure.asVanilla` /
`asAdventure`); Paper maps them directly with its own codecs.

### Chat: `AsyncChatEvent`

`FusionCoreChatProcessor` is a port of Paper 1.21.1's `ChatProcessor`, called from
`ServerGamePacketListenerImplMixin#chat` in place of Spigot's `AsyncPlayerChatEvent`-only code:

1. NeoForge's `ServerChatEvent` has already run in `handleChat`; a mod that cancels it stops
   the message. Its edits reach Bukkit listeners (upstream Arclight gave them the raw text).
2. `AsyncPlayerChatEvent` and the sync `PlayerChatEvent`, only while something listens.
3. `AsyncChatEvent`, carrying over cancel state, recipients, message and format.
4. The deprecated sync `ChatEvent`, only while something listens.

The message is sent once, at the end. Default renderer: the signed message with vanilla
`ChatType.CHAT` (team colours kept while the display name is unchanged). Custom renderer: per
viewer, with chat type `fusioncore:raw` (`data/fusioncore/chat_type/raw.json` in the mod jar,
Paper's `paper:raw`); if a world lacks it, system messages, as Spigot does. Paper's
`PlayerEvent(Player, boolean)` constructor is package-private in Spigot, so `EventAccessor`
sets `Event.async` instead. Not provided: `AsyncChatDecorateEvent`, `Player#displayName()`,
`deleteMessage`.

### Other additions

| Added | Where | Without it |
|---|---|---|
| `Audience` on `CommandSender` | `CommandSenderMixin` | `NoClassDefFoundError: net/kyori/adventure/...` |
| `AsyncChatEvent`, `ChatEvent`, `ChatRenderer` | `io.papermc.paper.*`, `FusionCoreChatProcessor` | listener not registered |
| `Chunk#getTileEntities(Predicate, boolean)`, `(boolean)` | `ChunkMixin`, `CraftChunkMixin` | `NoSuchMethodError` on every call |
| `PlayerJoinEvent#joinMessage`, `PlayerQuitEvent#quitMessage` | `event.*EventMixin` | `NoSuchMethodError` |
| `SignChangeEvent#lines()`, `line(int[, Component])` | `event.SignChangeEventMixin` | `NoSuchMethodError` |
| `TextDisplay#text()` | `TextDisplayMixin`, `CraftTextDisplayMixin` | `NoSuchMethodError` |
| `Plugin#getSLF4JLogger()` | `PluginMixin` | `NoSuchMethodError` while enabling |
| `Server#getCommandMap()` | `ServerMixin` | `NoSuchMethodError` while enabling |
| `CommandMap#getKnownCommands()` | `CommandMapMixin` + `SimpleCommandMapAccessor` | `NoSuchMethodError` while enabling |
| `com.destroystokyo.paper.utils.PaperPluginLogger` | stand-in class | `NoClassDefFoundError` before `onEnable` |
| `AsyncTabCompleteEvent` (players and console) | `ServerGamePacketListenerImplMixin_TabComplete`, `ArclightTerminalReader` | listener not registered |
| `PrepareResultEvent`, `com.destroystokyo…PrepareGrindstoneEvent` | `FusionCorePrepareResult`, menu mixins | listener not registered |
| `Advancement#getDisplay()` → `io.papermc.paper.advancement.AdvancementDisplay` | `PaperCallTransformer` → `FusionCorePaperCalls` | `NoSuchMethodError` |
| `Bukkit.broadcast(Component[, String])`, `Server#broadcast(…)` | `PaperCallTransformer`, `ServerMixin` | `NoSuchMethodError` |
| `Entity#teleportAsync(Location[, TeleportCause[, TeleportFlag...]])`, `teleport(…, TeleportFlag...)` | `EntityMixin`, `FusionCoreTeleport` | `NoSuchMethodError` |
| `World#getChunkAtAsync(…)` / `getChunkAtAsyncUrgently(…)` | `WorldMixin` | `NoSuchMethodError` |
| `PlayerAdvancementDoneEvent#message()`, `message(Component)` | `event.PlayerAdvancementDoneEventMixin`, `PlayerAdvancementsMixin` | `NoSuchMethodError` |
| `PhantomPreSpawnEvent`, `PreCreatureSpawnEvent`, `Phantom#getSpawningEntity()` | `PhantomSpawnerMixin`, `PhantomMixin` (core and Bukkit), `CraftPhantomMixin` | listener not registered, `NoSuchMethodError` |
| `AsyncPlayerSendCommandsEvent`, `AsyncPlayerSendSuggestionsEvent`, `io.papermc.paper.command.brigadier.CommandSourceStack` | `CommandsMixin#sendCommands`, `ServerGamePacketListenerImplMixin_TabComplete`, `CommandSourceStackMixin_Paper` | listener not registered |
| `PaperServerListPingEvent`, `StatusClient`/`NetworkClient`, `com.destroystokyo.paper.profile.PlayerProfile`/`ProfileProperty`, `CachedServerIcon#getData()`/`isEmpty()` | `FusionCoreServerListPing`, `ServerStatusPacketListenerImplMixin`, `ConnectionMixin`, `ServerHandshakePacketListenerImplMixin`, `CachedServerIconMixin`, `CraftIconCacheMixin` | listener not registered; SuperVanish falls back to ProtocolLib (below) |
| Folia schedulers (`io.papermc.paper.threadedregions.scheduler.*`), `Server`/`Bukkit` `get{Global,Async}RegionScheduler`/`getAsyncScheduler`, `isOwnedByCurrentRegion(…)`, `Entity#getScheduler()`; `io.papermc.paper.math` (`Position`, `BlockPosition`, `FinePosition`), `Location` as a `FinePosition` | `mod.server.scheduler.*`, `ServerMixin`, `EntityMixin` + `CraftEntityMixin` (Bukkit), `LocationMixin`, `SimplePluginManagerMixin`, `FusionCoreBukkit`; hooks in `MinecraftServerMixin`, core `EntityMixin`, `PlayerListMixin` | plugins fall back to the Bukkit scheduler, or fail with `NoClassDefFoundError` |
| `Plugin#getComponentLogger()` (library `adventure-text-logger-slf4j` 4.17.0 added to the server) | `PluginMixin` | `NoSuchMethodError` once a plugin takes its Paper path |
| `com.destroystokyo.paper.Title`, `Player#sendTitle(Title)`/`updateTitle`/`hideTitle`, BungeeCord-component `showTitle`/`setSubtitle`/`setTitleTimes`/`setPlayerListHeaderFooter` | `PlayerMixin`, `CraftPlayerMixin`, `FusionCoreTitles` | `NoClassDefFoundError` / `NoSuchMethodError` |
| `Plugin#getPluginMeta()` (`PluginDescriptionFile` as `PluginMeta`), `io.papermc.paper.ServerBuildInfo`, `Server`/`Bukkit` `getMinecraftVersion()`, `Bukkit.getVersionMessage()` | `PluginMixin`, `PluginDescriptionFileMixin`, `FusionCoreBuildInfo`, `ServerMixin`, `FusionCoreBukkit` | `NoSuchMethodError` / `NoClassDefFoundError` |
| `Server`/`Bukkit` `getTPS`, `getTickTimes`, `getAverageTickTime`, `getCurrentTick`, `isStopping`, `getPluginsFolder`, `getWorld(NamespacedKey)`/`getWorld(Key)`, `getOfflinePlayerIfCached`; `Bukkit.getCommandMap()`; `NamespacedKey` as an Adventure `Key` | `ServerMixin`, `FusionCoreBukkit`, `FusionCorePaperServer`, `NamespacedKeyMixin`, `GameProfileCacheAccessor` | `NoSuchMethodError` |

Two of these cannot live on the Bukkit types: Paper's `getDisplay()` differs from Spigot's
only in return type, and `Bukkit.broadcast` is static. `PaperCallTransformer` rewrites plugin
bytecode calls to them into static calls on `FusionCorePaperCalls`, the mirror image of how
Paper's Commodore rewrites Spigot plugins' `getDisplay()`. `teleportAsync` and
`getChunkAtAsync` run on the main thread (right away, or on the next tick from another thread)
and complete there, as Paper's futures do, but without Paper's off-thread chunk loading (the
`TeleportFlag` overload and the deprecated `ChunkLoadCallback` overloads came later). The
advancement event carries the vanilla announcement and the announcement lambda in
`PlayerAdvancements#award` (matched by descriptor) sends whatever listeners left, `null` for
none.

`AsyncTabCompleteEvent` follows Paper: with listeners present, suggestion packets are handled on
a 4-thread pool; a cancelled event sends nothing, a handled one sends its completions, otherwise
the packet takes the normal main-thread path. `PrepareResultEvent` fires after Spigot's own prepare
events, which keep their own handler lists (in Paper the anvil, grindstone and smithing events
extend it); it shares `PrepareInventoryResultEvent`'s list, as in Paper.

`PhantomPreSpawnEvent` fires before each phantom is created, after NeoForge's
`PlayerSpawnPhantomsEvent`. A cancelled spawn creates no entity; with `shouldAbortSpawn` the
player's remaining phantoms that tick are skipped, which is what Paper's `break` does. The phantom
keeps the player's UUID under Paper's NBT key `Paper.SpawningEntity`. `PreCreatureSpawnEvent` exists
only as the parent class with the shared handler list: Paper also fires it for natural and spawner
spawns, Fusion Core does not.

A server list ping fires `PaperServerListPingEvent` instead of Spigot's `ServerListPingEvent`, as
Paper does (`StandardPaperServerListPingEventImpl`); it is a subclass and shares the handler list, so
Spigot listeners still get it. Without it SuperVanish hid vanished players by rewriting the status
packet through ProtocolLib, which re-encoded it with an empty favicon (`data:image/png;base64,`, the
client logs "Failed to decode server icon") and without NeoForge's `isModded`. The initial sample is
built as upstream Arclight built it; `getHostname()` keeps Spigot's value instead of Paper's empty
string; the event's Adventure-MOTD constructor and Paper's legacy (pre-1.7) ping event are not
provided. The client's protocol version and virtual host come from the handshake. Paper's
`PlayerProfile` interface exists for this event's player sample; there is no profile implementation
backed by a real game profile yet (`Bukkit.createProfile` and friends are still missing).

`AsyncPlayerSendCommandsEvent` hands plugins the Brigadier tree about to be sent to a player, before
`PlayerCommandSendEvent`, as in Paper. Plugins that hide commands or arguments this way need not parse
the commands packet, which fails on a modded server: mod argument types (Create's and others) are
encoded in formats only the mod knows, so packetevents/ProtocolLib cannot read the packet. Arclight
builds the tree on the main thread, so the event fires once, synchronously, with
`hasFiredAsync() == false` (Paper fires an async and a sync copy; listeners written as Paper advises
handle both). Replies to suggestion requests, vanilla's and `AsyncTabCompleteEvent`'s, go through
`AsyncPlayerSendSuggestionsEvent`, cancelled up front when empty, as in Paper and CraftBukkit (so an
empty reply is no longer sent). Minecraft's `CommandSourceStack` implements Paper's interface of the
same name. Paper's command registration API (`Commands`, lifecycle events, `CommandRegisteredEvent`)
and the deprecated `BukkitBrigadierCommandSource` are not provided.

Before plugins load, `net.kyori.ansi.colorLevel` is set from the console's ANSI support unless
the user set it: plugins that shade `net.kyori.ansi` without jansi (AdvancedBanX) otherwise probe
jansi 2 and hit the jansi 1.x inside jline 2.

Limits: `getTileEntities(..., false)` still returns snapshots (Spigot has no live
`BlockState`s). Join/quit messages and sign lines are stored as legacy text, so RGB survives
and click/hover do not.

`Server#getCommandMap()` is an interface default that really runs: `CraftServer`'s own
accessor returns the narrower `SimpleCommandMap`, so its descriptor differs and it does not
override. `CommandMap#getKnownCommands()` is needed so that the interface call resolves at
all; on the server's own `CraftCommandMap`, CraftBukkit's existing public
`getKnownCommands()` has the same descriptor and is the one selected. The default body,
which reads Spigot's protected `knownCommands` field through an accessor mixin, is only a
fallback for other `SimpleCommandMap` subclasses.

`getSLF4JLogger()` is declared on `Plugin`, as in Paper, so calls made through the `Plugin`
type resolve too. The logger is named after the plugin.yml `prefix`, falling back to the
plugin name, like Arclight's plugin logger. `PaperPluginLogger.getLogger` hands back that
same logger through `ArclightPluginLogger`, so it follows Arclight's log4j routing.

The Folia scheduler API is Paper's patch 0843 ("Folia scheduler and owned region API"), as Paper
(not Folia) runs it: one tick thread, so every region belongs to the main thread,
`isOwnedByCurrentRegion` is "am I on the main thread", region tasks are global tasks, and the global
and entity schedulers tick on the main thread right after the Bukkit scheduler. Entity tasks run
while the entity is in a world (Arclight's `valid` flag), follow it across dimensions (the
`CraftEntity` is carried over), and get their "retired" callback when it leaves the server:
removal for any reason but a dimension change, or a player quitting. A disabled plugin loses its
global and async tasks. Deviations: Paper creates an entity scheduler with every `CraftEntity` and
ticks all entities of all worlds every tick; here it is created on first use (starting retired if
the entity is already gone) and ticked only while it holds tasks, which runs tasks on the same
ticks. The async scheduler's threads are daemon threads, so a pending plugin timer cannot keep the
JVM alive after the server stops. Plugins commonly detect "modern Paper" by the presence of these
classes (UltimateServerProtector checks for `AsyncScheduler`) and then use more Paper API; that is
why `getComponentLogger()` came with them. The component logger uses Adventure's default provider
(plain text after `GlobalTranslator`), not Paper's, which also resolves vanilla translation keys:
Adventure looks its provider up through the class loader of the library, which cannot see
Fusion Core's classes.

The BungeeCord-component title API is Paper's patches 0024/0059, deprecated in Paper since 1.16.5
and still used (AdvancedBanX, DeluxeMenus, PlaceholderAPI, PlasmoVoice). `Title#send` calls the same
code as `Player#sendTitle` directly: javac does not see the method a mixin adds to `Player`.

`ServerBuildInfo` reports brand `fusioncore:fusioncore` ("Fusion Core"), Minecraft 1.21.1, no build number,
the git commit from the jar's `Implementation-Version` and the JVM start as build time (Paper's fallback
without a manifest), so `asString` gives `1.21.1-DEV-<commit>`. `buildInfo()` returns it directly instead
of Paper's `ServiceLoader` lookup. Deviation: `isBrandCompatible` also accepts Paper's brand, as Paper's
own forks do, since plugins ask it to find out whether Paper API is available.

`getTPS` returns Spigot's `recentTps` (what `/tps` shows), which now starts at 20 like Paper's rolling
averages instead of 0 before the first sample. `getTickTimes`/`getAverageTickTime` read vanilla's 100-tick
ring buffer, which is what Paper's `tickTimes5s` duplicates. `getCurrentTick` is Arclight's copy of
CraftBukkit's wall-clock tick counter, which now starts at class load as CraftBukkit's does (it was 0
until the first tick). `getOfflinePlayerIfCached` reads the profile cache without a web request, like
Paper, but leaves CraftServer's private offline-player cache alone.

Paper's statics on Bukkit classes are declared in holders (`FusionCoreBukkit` for `Bukkit`,
`FusionCoreCommandStatics` for `Command`, `FusionCoreItemStackStatics` for `ItemStack`) under the same names and
descriptors; `PaperCallTransformer` points any plugin `INVOKESTATIC` of such a method that the Bukkit class lacks
at its holder. Paper-only constructors (`new BroadcastMessageEvent(boolean, Component, Set)`, `new
PlayerKickEvent(…, Cause)` and the like) are rewritten from `NEW; DUP; args; INVOKESPECIAL` into a call of
`FusionCoreEvents.new<Class>(args)`, which builds the object with Spigot's constructor; the rewrite leaves the code
alone if a stack map frame lies between `NEW` and the constructor call.

### Adventure

Paper's Adventure API (patches 0006/0010 "Adventure") on items, books, entities, players, command senders,
signs, command blocks, commands, events, scoreboards, the server and worlds (both are audiences: console plus
online players, the players of a world), inventories and merchants with component titles, `Keyed.key()`,
`Translatable.translationKey()`, `Registry.get(Key)`, rich-message config accessors, `UnsafeValues`
serializers and `resolveWithContext`, advancement and enchantment names. Deviations:

- Where Spigot stores legacy text (display and list names, team prefixes, objective names, sign lines, event
  messages, MOTD, inventory titles, map cursor captions), the component accessors convert: colours and RGB are
  kept, click/hover events are lost. Item names/lore, book pages, entity custom names and kick screens keep
  full components.
- Legacy text now resolves vanilla translation keys like Paper's `PaperAdventure.FLATTENER` (console shows
  `[Stone]`, not `chat.square_brackets`). Adventure's own serializers obtained by plugins do the same
  (see "Adventure service providers").
- `BookMeta` is an Adventure `Book`; Paper's `title/author/pages(...)` return `BookMeta`, which javac cannot
  express over the mixin, so plugin calls with those descriptors (and `BookMetaBuilder#build`) are rewritten
  to `FusionCorePaperCalls`.
- `PlayerKickEvent#getCause` / `PlayerGameModeChangeEvent#getCause`: `PLUGIN` for kicks and game mode changes
  requested through the Bukkit API, `COMMAND` for `/gamemode`, `UNKNOWN` otherwise (Paper names each vanilla
  kick reason). A cancelled game mode change sends the event's cancel message, as in Paper.
- `UnsafeValues#resolveWithContext` ignores `bypassPermissions` (Paper's is a field of its own on the command
  source).

### Adventure service providers

Paper registers its Adventure providers in `META-INF/services` (patch 0010, `io.papermc.paper.adventure.providers`).
Adventure looks them up with `ServiceLoader` on its own class loader; here Adventure is an installer library in the
launcher layer and cannot see the game layer where the server lives, so plugins got Adventure's defaults:
`plainText()`/`legacySection()` printed translation keys, legacy dropped RGB, `ClickEvent.callback` answered "Callbacks
are not supported on this platform!", `BossBar#viewers()` was always empty, SNBT data component values did not convert.

The providers are in the launcher jar (`bootstrap/src/main/java/io/izzel/arclight/boot/adventure/FusionCoreAdventureBridge`,
listed in the launcher jar's `META-INF/services`, which puts them in the same layer as Adventure). They forward on every
use to what the server installs at start (`FusionCoreAdventureProviders#install`): Paper's flattener, the SNBT legacy hover
serializer, boss bar viewers, click callbacks and data component conversions. Before that the flattener is Adventure's
basic one. Same as Paper: plain/legacy/Gson/MiniMessage/component logger/boss bars/click callbacks (`/paper:callback
<uuid>`, queued and expired on the server tick). Deviations:

- Data component values: Paper converts its own value type to and from JSON and SNBT; Fusion Core's item hovers are JSON
  already, so the conversion is SNBT <-> JSON directly (through the component type's codec).
- The Fabric jar has no launcher-side code of this kind and keeps Adventure's defaults.
- Adventure 4.17 only writes legacy hover values through the legacy hover serializer; it never reads a legacy
  `show_item`/`show_entity` value (the branch only runs for component-typed actions), on Paper as well.

`Player` is an Adventure `BossBarViewer` (Paper's API), and `showBossBar` creates the bar's implementation as Paper does,
so `viewers()` answers.

### Entity API, third batch

Paper's plain entity state (patches 0185, 0209, 0218, 0240, 0254, 0268, 0348, 0355, 0403, 0419, 0437, 0452, 0457, 0557, 0577,
0591, 0638, 0672, 0674, 0700, 1035) as default methods of interface mixins on the Bukkit types (`mixin/bukkit/<Type>Mixin`).
Private vanilla fields are opened in `fusioncore.accesswidener`; non-public methods go through invoker mixins
(`mixin/core/world/entity/api`), because widening a method that mods override is unsafe. Deviations:

- Paper adds small helpers to vanilla classes (`AbstractHorse#setForceStanding`, `Zombie#stopDrowning`, `EyeOfEnder#signalTo(pos,
  update)`, `Goat#ram`, `FishingHook#resetTimeUntilLured`, `AbstractArrow#setOwner(entity, resetPickup)`); Fusion Core does the same
  steps from the API method. `EnderSignal#setTargetLocation(location, false)` restores the eye's life and drop decision after
  `signalTo`, so the random source advances once more than on Paper.
- `ZombieVillager#setConversionTime(time, false)` suppresses the entity event inside vanilla's `startConverting`.
- `Firework`: Paper dropped CraftBukkit's item mirror; here `getFireworkMeta`/`setFireworkMeta` are overwritten to use the
  entity's current item. CraftBukkit's constructor still turns a non-rocket item into a rocket when the Bukkit entity is created
  (Paper does not).

### Entity API with core hooks

Paper state that changes vanilla behaviour, kept on the vanilla entity by mixins in `mixin/core/world/entity/api` and saved
under Paper's NBT keys: `shouldBurnInDay` for zombies (through `isSunSensitive`, so husks keep never burning), skeletons and
phantoms (`Paper.ShouldBurnInDay`, patches 0268, 0528); the tadpole age lock (`AgeLocked`, also in buckets, 0557); wither portal
travel and the bee rolling override (not saved, as in Paper, 0557); falling block auto expire (`Paper.AutoExpire`, 0700); the
entity that launched a firework (`SpawningEntity`: crossbow shooter, player placing it or boosting an elytra flight, 0121); the
ender dragon podium, used by the dragon and its phases instead of the podium at the fight origin (not saved, as in Paper, 0687).
Vanilla AbstractSkeleton has no save override; the mixin adds one, as Paper does.

### Entity API with core hooks, last batch

- **Experience orb reasons** (patch 0125): reason, trigger and source on every orb, saved as `Paper.ExpData`. Paper adds
  `ExperienceOrb` constructors and `award` overloads; mods keep calling the vanilla ones, so Fusion Core runs each vanilla orb source
  inside a thread-local context (`FusionCoreExperienceOrbs`) that the orb constructor reads: deaths (PLAYER_DEATH/ENTITY_DEATH, the
  dragon's gradual experience too), breeding (animals, foxes, turtles), villager and wandering trader trades, fishing, bottles,
  grindstone (also when NeoForge's `onGrindstoneTake` drops it), furnaces, blocks (`Block#popExperience`, NeoForge's
  `handleBlockDrops` with the breaker as trigger, Fusion Core's leftover BlockBreakEvent experience), CUSTOM for orbs spawned by
  plugins. Orbs from mods' own code are UNKNOWN unless they go through one of those paths.
- **Villagers**: reputation (0354) over the villager's gossips; `increaseLevel`/`addTrades` (0764). NeoForge fills the trade table
  for every profession, with empty levels for the jobless and the nitwit, so empty levels count as "no trades" (Paper checks only
  for a missing table).
- **Paper's capability interfaces**: `Bucketable` (axolotl, fish; 0643), `Shearable` (sheep, mooshroom, snow golem, bogged; 0802),
  `Leashable` (mobs keep Spigot's methods, boats get Paper's; 1042 — Paper's world-generation check is left out for boats),
  `Frictional` (living entities through `shouldDiscardFriction`, items through their ground friction; `Paper.FrictionState`; 0769),
  `CollarColorable` (wolf, cat) with `EntityDyeEvent` fired when a player dyes a collar (0695).
- **Default entity attributes** (0584): `EntityType#hasDefaultAttributes/getDefaultAttributes`, `UnsafeValues` counterparts, a
  read-only view of the attribute supplier (mods' suppliers included). `Attributable#registerAttribute` itself is not there yet.

### Block state API

Paper's block state additions, with the state Paper keeps on the vanilla block entity added by mixins
(`mixin/core/world/level/block/entity`, `.../api`) and saved under Paper's NBT keys; bodies in `FusionCoreBlockStates`, reading the
state's snapshot or the live block entity as Paper's Craft class does:

- **Campfire** per-slot cooking switch (`Paper.StopCooking`, patch 0634): a stopped slot keeps its item and progress.
- **Furnace** cook speed multiplier (`Paper.CookSpeedMultiplier`, 0247): scales vanilla's total cook time, the total that
  `FurnaceStartSmeltEvent` starts with, and a smelt finishes once progress reaches the total (Paper's `>=`); used recipes (0664).
- **Hopper** transfer cooldown (0932); **Sign** allowed editor and interactable side (0833); **Chest/EnderChest** `isBlocked`
  (0963); **Lidded** `isOpen` = opened by the API (0541); **Beehive** `clearEntities` (0524); **Sculk sensor** listener range
  (`Paper.ListenerRange`, saved only when it differs from the block's own 8/16, 0665); **Sculk catalyst** `bloom(Position, int)`
  (0855); **MovingPiston** (0757); **Spawner** `setSpawnedItem` for block spawners and spawner minecarts (0286: the next spawn
  data only, no block update packet from the snapshot); **DragonBattle** gateways and crystals (0867); **LimitedRegion**
  block states, scheduled ticks, world and center (0555).
- **Supertypes**: `Container`/`Beacon` are `LockableTileState`, `Container`, `Lectern`, `Jukebox`, `DecoratedPot`,
  `ChiseledBookshelf` are `TileStateInventoryHolder` (API 0380, 0488). Jukebox, pot and bookshelf return their own inventory
  types, so their mixins add the `Inventory`-returning `getSnapshotInventory` the new supertype needs.
- `TileState#isSnapshot` is always true: Fusion Core's states are always snapshots.
- **Block data** bounds and setters (0666: levelled, candles, leaves distance, petals, bed occupied, pot cracked) look the
  property up on the block by its vanilla name, so modded blocks that reuse the name work too; Paper reads the generated
  Craft class's static property.

### Inventory API

Slot accessors of the anvil, smithing table, stonecutter, grindstone, cartography table and lectern inventories (API patch
0185); `FurnaceInventory#isFuel/canSmelt` (0642; Paper asks the recipe manager for the furnace's recipe type, a NeoForge-only
field, so Fusion Core asks the furnace's own cached recipe check, the same lookup); `Inventory#close` (0527) and
`removeItemAnySlot` (0238); `EntityEquipment#get/setDropChance(EquipmentSlot)` through vanilla's per-slot getter and setter,
so body armor has its own chance (0512; Spigot's private helpers map BODY onto the feet); `EquipmentSlot#isHand/isArmor/
getOppositeHand` (0365); `AnvilView#bypassEnchantmentLevelRestriction` (1066) and `EnchantmentView#setEnchantmentSeed` (1047).
Horse inventories are `SaddledHorseInventory`, the horse's also `ArmoredHorseInventory` (0062/0142), declared on the Craft
classes (`CraftInventoryAbstractHorse`, `CraftInventoryHorse`); the Bukkit interfaces themselves keep Spigot's supertypes.

### Item meta and recipe API

- `ItemMeta` can-place-on/can-destroy (server patch 1037): Paper moved the `can_place_on`/`can_break` components into
  fields of `CraftMetaItem`; Spigot keeps them among the unhandled tags, and Fusion Core reads and writes them there, so
  equals/hashCode/clone/serialisation stay Spigot's. An existing component keeps its tooltip flag; a new one is hidden by
  `HIDE_PLACED_ON`/`HIDE_DESTROYS`, as Paper does. An empty set removes the component. `NamespacedKey` is a
  `com.destroystokyo.paper.Namespaced`, as in Paper.
- `Damageable#hasDamageValue/resetDamage` (0960): Spigot keeps the damage as an int written only above 0, so "has a value"
  is "damage > 0" (Paper keeps an explicit 0). `CompassMeta#isLodestoneCompass/clearLodestone` (0960): true when Spigot
  writes the tracker (a target, or tracking off); a tracker with neither does not survive Spigot's meta.
- `LeatherArmorMeta#isDyed` for leather and colorable armour (0961), `BlockStateMeta#clearBlockState` for block-state
  items and shields (0377), `SpawnEggMeta#get/setCustomSpawnedType` (0803; Paper resolves only `minecraft:` ids, Fusion Core
  any id the entity registry knows, so mod entities work), `ArmorStandMeta` on the armor stand item (0169; Paper also
  changed its YAML form, Fusion Core keeps Spigot's "internal" blob, which carries the same entity data),
  `ItemFactory#hoverContentOf` (0405, same bodies; items throw as in Paper).
- `MerchantRecipe` ignore-discounts (0425): the flag on the Bukkit recipe, Paper's constructors and copy constructor
  (factories `FusionCoreEvents.newMerchantRecipe`), on the vanilla offer (copied by `MerchantOffer#copy`, saved under Paper's
  key `Paper.IgnoreDiscounts` by wrapping `MerchantOffer.CODEC`), and skipped by the villager's reputation and Hero of
  the Village discounts.
- Smithing recipes with `copyDataComponents` (0579): the flag on Bukkit's `SmithingRecipe`, Paper's constructors
  (`FusionCoreEvents.newSmithing*Recipe`), carried to the vanilla recipe by the Craft recipes and back by `toBukkitRecipe`;
  without it the transform does not apply the result's components and the trim starts from a fresh stack, as in Paper.
- `RecipeChoice.empty()` and `validate` (0966): `org.bukkit.inventory.EmptyRecipeChoice` is public here (Paper's is
  package-private) so the static holder can return it. As in Paper, an empty vanilla ingredient converts to the empty
  choice instead of null (smithing templates, cooking inputs); shaped recipes skip it for blank slots. Paper's Bukkit
  constructors that replace a null smithing choice with the empty one, and the `validate` calls inside Bukkit's recipe
  classes, are not ported: Spigot's classes keep null, which the core still converts to an empty ingredient.
- `ShapedRecipe#setIngredient(char, ItemStack)`, `ShapelessRecipe#removeIngredient(ItemStack)` and `(int, ItemStack)` (0160).
- The recipe on `BlockCookEvent`/`FurnaceSmeltEvent` (0497), set by the campfire and furnace hooks (the campfire asks the
  recipe manager, the vanilla lookup sits in a lambda); Paper's constructors with the recipe and `FurnaceStartSmeltEvent`
  with the cook time are factories in `FusionCoreEvents`.

### Replenishable lootables

- Paper's `LootableInventory`/`LootableBlockInventory`/`LootableEntityInventory` and `LootableInventoryReplenishEvent`
  (API patch 0039, server patch 0095). Chest, barrel, crafter, dispenser, dropper, hopper and shulker box states,
  storage/hopper minecarts and chest boats have the API; `Lootable` gets `setLootTable(table, seed)`, `hasLootTable` and
  `clearLootTable` (a mob falls back to its type's table, as in Paper).
- The replenish data (`FusionCoreLootables.Data`, Paper's `PaperLootableInventoryData`) lives on
  `RandomizableContainerBlockEntity`, `AbstractMinecartContainer` and `ChestBoat` and is saved under Paper's key
  `Paper.LootableData`, so worlds move between Paper and Fusion Core with it. The vanilla default methods of
  `RandomizableContainer` (`tryLoadLootTable`, `trySaveLootTable`, `unpackLootTable`) and `ContainerEntity` (save, load,
  unpack) are overwritten with Paper's versions: as in Paper, holders with the data save and load their items even while a
  loot table is set. Holders without the data (decorated pots, modded containers that implement the vanilla interfaces
  themselves) keep vanilla behaviour, where Paper throws for a `ContainerEntity` without it.
- Shulker boxes as in Paper: a player breaking one in creative drops the loot table even if it could refill; broken
  otherwise, a box already filled drops without its loot table.
- Settings: Paper's per-world `lootables` section is one `lootables` section in `fusioncore.conf` for all worlds, with
  Paper's names and defaults (`auto-replenish = false`, so nothing changes until it is turned on) and Paper's duration format.
- Deviation: Spigot has no live block states, so `LootableInventoryReplenishEvent#getInventory` for a block is a snapshot
  (Paper hands the live state); changes made through it need `update()`. The API on a block state reads and writes the
  state's snapshot and reaches the block with `update()`, as in Paper.

### Server, UnsafeValues and permissions

- `Server`/`Bukkit`: `addRecipe(recipe, resend)`, `removeRecipe(key, resend)`, `updateRecipes`, `updateResources` (patch
  0845: the recipe packet and recipe books, or the whole `/reload` resend), `broadcast(BaseComponent...)`,
  `createExplorerMap` with the non-legacy `StructureType` (0857), `getPermissionMessage` (0266), `getPlayerUniqueId` (0177;
  "online" when the server or its proxy authenticates, with Paper's proxy defaults), `isTickingWorlds` (0707; true from the
  first world tick to the end of the world loop of `tickChildren`, which works the same on every loader),
  `reloadCommandAliases` (0111), `reloadPermissions` (0068; permissions.yml after the plugins' permissions, Spigot's
  order, i.e. Paper's `load-permissions-yml-before-plugins = false`), `suggestPlayerNamesWhenNullTabCompletions` (Paper's
  default, true), `Server.Spigot#getBukkitConfig`. Bukkit's statics are in `FusionCoreBukkit`.
- `createCommandSender` (0658): `FusionCoreFeedbackSender`, an operator whose Bukkit and vanilla command feedback goes to the
  consumer (`VanillaCommandWrapper#getListener` hands vanilla commands its own source). Deviation: Paper shares the
  console's `PermissibleBase`; here the sender has its own, so permissions attached to the console do not carry over.
- `UnsafeValues`: entity bytes (`serializeEntity`/`deserializeEntity`, 0593; passengers dropped from the data as Paper does,
  older data updated by the DataFixer), `getMainLevelName`, `getSpawnEggLayerColor` (mod eggs too), `getStatisticCriteriaKey`,
  `nextEntityId` (vanilla's counter, widened), `isSupportedApiVersion`, `setBiomeKey`, and the static `isLegacyPlugin`
  (holder `FusionCoreUnsafeValuesStatics`). `getVersionFetcher` does not query PaperMC: it reports the Fusion Core build, as
  `/version` does.
- `PluginManager` is a Paper `PermissionManager`; `SimplePluginManager` gets `addPermissions` (one recalculation) and
  `clearPermissions`. `overridePermissionManager` belongs to Paper's plugin manager and waits for the Paper plugin loading item.
- `BukkitScheduler#getMainThreadExecutor` (0488), `Command#canBeOverriden` (false, as for every non-Brigadier command in
  Paper), `ProxiedCommandSender` as a forwarding audience to its caller.

### Scoreboards, translation keys, types and small entity API

- Scoreboards: entity entries (`Objective#getScoreFor`, `Scoreboard#getScoresFor/resetScoresFor/getEntityTeam`,
  `Team#addEntity/removeEntity/hasEntity`, patch 0637; an entity's entry is vanilla's scoreboard name, the UUID for
  non-players), score state (`isTriggerable`/`setTriggerable`, `customName`, `resetScore`, `Objective` auto-updated display,
  patches 0592, 0912), team batches (`addEntries`/`removeEntries`/`addEntities`/`removeEntities`, patch 0649; Paper sends one
  team packet per batch, Fusion Core one per entry with the same end state), `Team#hasColor`.
- `DisplaySlot#getId` and `toString` (vanilla's slot id), `DisplaySlot.NAMES`. Paper renamed the team sidebars to
  `SIDEBAR_TEAM_*`: plugins compiled against Paper get Spigot's `SIDEBAR_*` constants, so `name()`/`valueOf` keep Spigot's
  names. Static fields whose value is not of the owner's type (`DisplaySlot.NAMES`, `FireworkEffect.Type.NAMES`) go through
  `FusionCoreEnumConstants` getters marked `@Of(owner)`.
- `translationKey()` (patch 0404) on `Difficulty`, `GameMode`, `GameRule`, `Biome`, `FireworkEffect.Type`, `CreativeCategory`,
  `MusicInstrument` and `Villager.Profession` (all Adventure `Translatable`, as in Paper); `GameMode#isInvulnerable`.
- `Material#isEmpty/isCollidable/getItemAttributes/getDefaultAttributeModifiers()`, `ItemType#getDefaultAttributeModifiers()`
  (all slots, patch 0539; a slot group Bukkit does not know, `body` for wolf armour, is reported as ANY) and
  `getItemRarity` (patch 1029), `BlockType#hasCollision` (patch 0590). Deviation: Paper's deprecated
  `Material#getItemRarity` reads a new stack's meta and throws for items without an explicit rarity; here it is the item's
  default rarity.
- `Chunk#getChunkKey` and the static `Chunk.getChunkKey(x, z)`/`(Location)` (holder `FusionCoreChunkStatics`);
  `getChunkSnapshot(..., includeLightData)` returns Spigot's snapshot, which always has light.
- `Damageable#heal` (patch 0557, fires `EntityRegainHealthEvent`; dragon parts heal the dragon), `Tameable#getOwnerUniqueId`,
  `Attributable#registerAttribute` (patch 0489), `launchProjectile(type, velocity, consumer)` on every `ProjectileSource`
  (patch 0674): the consumer now runs right before the projectile is added to the world, so `ProjectileLaunchEvent` sees its
  changes (it used to run after the launch).
- `Spawner#isActivated/resetTimer` (patch 0286), `Advancement#getParent/getChildren/getRoot` (patch 0586),
  `Particle#builder`, Paper's constructors `Vibration(destination, time)`, `AttributeModifier(key, amount, op)` and
  `MapCursor(..., Component)` (factories in `FusionCoreEvents`), `EntityEffect.TAMING_FAILED/TAMING_SUCCEEDED` (Spigot's
  `WOLF_SMOKE/WOLF_HEARTS`), `ItemFlag.HIDE_ITEM_SPECIFICS` (alias of `HIDE_ADDITIONAL_TOOLTIP`).

### Paper utilities, timings classes and small plugin API

- Pure Paper API copied as published: `Tick`, `TransformingRandomAccessList`, `JarManifests`, `CachedSizeConcurrentLinkedQueue`,
  `SneakyThrow`, `ProviderUtil`, `LibraryLoadingException`, `BufferedCommandSender`, the event executors
  (`MethodHandleEventExecutor`, `StaticMethodHandleEventExecutor`, `asm/ASMEventExecutorGenerator`, `ClassDefiner`,
  `SafeClassDefiner`) and `TimingsReportListener`.
- `EventExecutor.create(Method, Class)` and the interface field `EventExecutor.eventExecutorMap` (holder
  `FusionCoreEventExecutorStatics`, getter in `FusionCoreEnumConstants`), as in Paper. Only plugins that call `create` use these
  executors; Arclight's plugin loader keeps its own for `@EventHandler` methods. `RegisteredListener#getExecutor` (patch 0373).
- Timings stay off. The rest of Paper's public timings classes exist for plugins that name them, all doing nothing:
  `TimingsManager` (`FULL_SERVER_TICK`, `TIMINGS_TICK`, `PLUGIN_GROUP_HANDLER`, export settings, `getCommandTiming` = the no-op
  timing, `getPluginByClassloader` as in Paper), `FullServerTickHandler`, `TimingHistory` counters (never advanced),
  `TimedEventExecutor` (calls the wrapped executor), `TimingsCommand` (not registered; every subcommand but `off` says timings
  are unavailable), `Timings.generateReport(TimingsReportListener)` (sends the notice and calls `done()` with no URL at once, so
  a plugin waiting for the callback is not left hanging). `TimingHandler` is no longer an empty stand-in but a no-op `Timing`.
- `Command#timings` (Paper's `Timing` field, next to Spigot's `CustomTimingsHandler` field of the same name: fields differ by
  type) is the no-op timing from construction on; Paper sets it only when the command is registered. `Command#getTimingName`.
- `Plugin#getDataPath` (patch 0477), `Plugin#getLog4JLogger` (patch 0067, named like the SLF4J logger), `PluginClassLoader#getPlugin`,
  `MetadataStoreBase#removeAll(Plugin)` (patch 0034; `synchronized` like the store's other methods, Paper's is not),
  `YamlConfigurationOptions#codePointLimit` (patch 0361, applied to the loader before each `loadFromString`), Paper's
  constructors `UnknownDependencyException(Collection, String)` and `ChannelNameTooLongException(int, String)` (factories in
  `FusionCoreEvents`; Spigot's exception has no message constructor, so the latter is an anonymous subclass carrying Paper's message).
- Not done: `TagEntry` (needs `TypedKey`/`TagKey`, registries), Paper's plugin loader internals (`JavaPlugin#init` with
  `PluginMeta`, `PluginClassLoader` groups, `LibraryLoader` factories, `SimplePluginManager#paperPluginManager`,
  `overridePermissionManager`, `isTransitiveDependency`, `getLifecycleManager`) — Paper plugin bootstrap.

### Paper's enum values

- Values Paper adds to Bukkit enums are registered at startup (`BukkitRegistry#loadPaperEnumValues`, Arclight's `EnumHelper`)
  and reached by plugins through `FusionCoreEnumConstants`: `Boat.Status.NOT_IN_WORLD`, `BedEnterResult.OBSTRUCTED`,
  `PlayerFishEvent.State.LURED`, `TargetReason.TARGET_OTHER_LEVEL/TARGET_INVALID`, `SpawnReason.OMINOUS_ITEM_SPAWNER`,
  `EntityRemoveEvent.Cause.DISCARD`, `InventoryType.DECORATED_POT`, `PortalType.END_GATEWAY`, `ItemFlag.HIDE_STORED_ENCHANTS`,
  `EntityEffect.ARMADILLO_PEEK/BODY_BREAK`, `MinecraftExperimental.Requires.BUNDLE/TRADE_REBALANCE`. `EquipmentSlotGroup.BODY`
  is registered by name in the class's static initializer (`EquipmentSlotGroupMixin`), so vanilla's `body` group (wolf and horse
  armour) is no longer unknown to Bukkit (it used to be reported as ANY).
- Fired where Paper fires them: `OBSTRUCTED` for vanilla's obstructed bed (Spigot: `OTHER_PROBLEM`); `LURED` when a fish starts
  swimming to the hook (cancelling resets the wait); the target reasons of brain-driven mobs that drop their target
  (`StopAttackingIfTargetInvalid`: can't attack → `TARGET_INVALID`, tired of reaching → `FORGOT_TARGET`, dead → `TARGET_DIED`,
  other world → `TARGET_OTHER_LEVEL`; Spigot said `FORGOT_TARGET` unless the target died); `OMINOUS_ITEM_SPAWNER` for what an
  ominous item spawner drops; `DECORATED_POT` for a decorated pot's inventory (Spigot: `CHEST`); `HIDE_STORED_ENCHANTS` hides
  an enchanted book's stored enchantments, split from `HIDE_ADDITIONAL_TOOLTIP` as in Paper (a book whose stored enchantments
  are hidden now reads back with `HIDE_STORED_ENCHANTS`).
- `Boat#getStatus` before the boat's first tick is computed instead of throwing, and `NOT_IN_WORLD` for a boat outside a world.
- Not fired: `DISCARD` (Paper uses it for entities dropped after a ticking crash or a duplicate UUID, neither of which Fusion Core
  handles like Paper), `END_GATEWAY` (Paper's own portal events use it only in `EntityPortalEnterEvent` with a portal type, part of
  the portal event API still missing).

### Enchantments, UUID attribute modifiers, vanilla biome provider, game event tags, feature flags

- `Enchantment` (patches "More Enchantment API", "Add even more Enchantment API"; bodies in `FusionCoreEnchantments`):
  `isTradeable`, `isDiscoverable`, `getMin/MaxModifiedCost`, `getAnvilCost`, `getWeight`, `getActiveSlotGroups`, `getActiveSlots`;
  `getRarity` and `getDamageIncrease` throw as in Paper. `isTreasure` is Paper's `#minecraft:treasure` tag (Spigot: "not in the
  enchanting table"). Paper declares these abstract; a plugin's own `Enchantment` subclass gets `UnsupportedOperationException`.
  Not done: `getSupportedItems`, `getPrimaryItems`, `getExclusiveWith` (Paper's registry sets).
- `AttributeInstance#getModifier(UUID)`/`removeModifier(UUID)` (patch "Add UUID attribute modifier API", Paper's table of vanilla's
  pre-1.21 UUIDs in `FusionCoreAttributeMappings`; other UUIDs map to `minecraft:<uuid>`, as Spigot's UUID constructor of
  `AttributeModifier` names them). Default attributes refuse it.
- `WorldInfo#vanillaBiomeProvider` (patch "Expose vanilla BiomeProvider from WorldInfo"): for a world, its generator's biome
  source behind a plugin's chunk generator and biome provider (the vanilla source is kept on `CustomWorldChunkManager`); for the
  `WorldInfo` a plugin generator gets while `createWorld` runs, the dimension's generator and a random state from the seed.
- Game event tags (patch "Add GameEvent tags"): registry `"game_events"` in `Server#getTag/getTags` (`CraftGameEventTag`), constants
  `Tag.GAME_EVENT_*`/`REGISTRY_GAME_EVENTS`. `FeatureFlag.ALL_FLAGS` (patch "Add FeatureFlag API"): built from vanilla's flag
  registry, so a mod's flags are included (Paper lists vanilla's by hand); flags with a `FeatureFlag` constant are that constant.
- `NamespacedKey#equals/hashCode` are Paper's: equal to, and hashed like, any Adventure `Key` with the same namespace and value.
  With Spigot's, a map keyed by `Key` (`Index`, `ALL_FLAGS`) could not find an entry by `Key.key(...)`.

### Paper's block events

Fired from the core (bodies in `FusionCorePaperBlockEvents`), each built only when someone listens; with no listener the vanilla
path runs untouched.

- `BellRevealRaiderEvent` (patch 0548): per raider a resonating bell is about to light up. A mod entity in the raiders tag that is
  not a Bukkit `Raider` gets no event (Paper casts blindly).
- `BlockBreakBlockEvent` (patch 0578): blocks a piston destroys, and blocks a flowing fluid washes away, with the piston / the fluid
  block it came from as the source; the listeners' drops go on through the loader's own drop handling (NeoForge's
  `BlockDropsEvent`), not around it. Deviation: Paper fires it for water only; here for every fluid whose `beforeDestroyingBlock`
  drops the block (mods' fluids included; lava does not drop).
- `BlockBreakProgressUpdateEvent` (patch 0933): from `ServerLevel#destroyBlockProgress`, not for ids of client-side entities.
- `BlockFailedDispenseEvent` (patch 0463): empty dispenser/dropper; `shouldPlayEffect(false)` skips the click (and, for the
  dispenser, the `BLOCK_ACTIVATE` game event). Its `callEvent()` returns `shouldPlayEffect()` as in Paper.
- `BlockLockCheckEvent` (patch 0778): containers (`BaseContainerBlockEntity#canOpen`) and beacons; message and sound as Adventure
  (`container.isLocked`, `block.chest.locked`).
- `CompostItemEvent`/`EntityCompostItemEvent` (patches 0797, 0735): decided in `ComposterBlock#addItem` once the item's chance is
  known (vanilla's table or NeoForge's data map); the result steers vanilla's own branch, so the level rise, game event and
  scheduled tick stay vanilla's. A cancelled entity event leaves the composter and the item alone (player: the interaction is
  skipped; a farmer villager keeps its item). With a listener, `addItem` draws its random once more than vanilla.
- `DragonEggFormEvent` (patch 0483): on every dragon death, cancelled up front when vanilla would not place the egg; with a
  listener the event places the egg state instead of vanilla.
- `PlayerShearBlockEvent` (patch 0440): shearing a full beehive or carving a pumpkin; the listeners' drops fall instead of the
  honeycombs/seeds (extra pumpkin drops share the seeds' motion). Mods' shears count (NeoForge's item abilities).
- `TargetHitEvent` (patch 0448): the listener sets the signal strength; cancelled, the target neither powers nor awards the hit.

### Paper's entity events

Fired from the core (bodies in `FusionCorePaperMobEvents`), each built only when someone listens.

- `ElderGuardianAppearanceEvent` (0550): asked only for players the guardian would curse (vanilla's test first).
- `EnderDragonFlameEvent`, `EnderDragonShootFireballEvent`, `EnderDragonFireballHitEvent` (0211): cancelled, no breath cloud (the
  sitting-flaming phase ends), no fireball, no cloud and no sound on hit.
- `EndermanEscapeEvent` (0184): RUNAWAY (sunlight), INDIRECT (projectiles/potions, asked once per hit, not per teleport attempt),
  STARE.
- `EntityDamageItemEvent` (0574): item damage of any non-player living entity (NeoForge already passes the entity to
  `hurtAndBreak`). Not done: the `damage_item` enchantment effect of a non-player owner, which vanilla routes through the player
  overload with no entity (Paper passes the owner).
- `EntityFertilizeEggEvent` (0795): frogs, sniffers, turtles; cancelled, nothing is bred and both lose their love; the event's
  experience drops (none at 0).
- `EntityLoadCrossbowEvent` (0459): cancelled, the crossbow stays empty; `setConsumeItem(false)` loads it without using ammo (and,
  as in Paper, without the charged sound). Not fired on the client side.
- `EntityPortalReadyEvent` (0722, nether portals): the listener may change or clear the target world; cancelled, the entity's
  portal process is dropped.
- `EntityTeleportEndGatewayEvent` (0194): instead of `EntityTeleportEvent` for a non-player going through an end gateway; a null
  `getTo()` counts as cancelled (for both events).
- `EntityToggleSitEvent` (0755): tamed animals (`setInSittingPose`), foxes, pandas, camels (only for a real change); not for the
  state loaded from NBT.
- `PufferFishStateChangeEvent` (0544): cancelled, no sound, no new state and the timer does not advance (asked again next tick).
- `ShulkerDuplicateEvent` (0924), `TameableDeathMessageEvent` (0682, message as Adventure), `WardenAngerChangeEvent` (0709, from
  the warden's own `increaseAngerAt`; `Warden#increaseAnger` of the API writes the anger table directly, as in Paper).
- `ThrownEggHatchEvent` (0315): every egg, dispensers too, after `PlayerEggThrowEvent`.
- `TurtleStartDiggingEvent`, `TurtleLayEggEvent`, `TurtleGoHomeEvent` (0254). Arclight's copy of the egg-laying goal also lost
  vanilla's laying sound and used 200 ticks instead of vanilla's `adjustedTickDelay(200)`; both restored.

### Paper's player events

Fired from the core (bodies in `FusionCorePaperPlayerEvents`, client options in `FusionCorePlayers`), each built only when someone
listens.

- `PlayerLaunchProjectileEvent` (0213): snowballs, eggs, ender pearls, experience bottles, splash/lingering potions, wind charges,
  tridents, fireworks launched from a block. Cancelled, the use fails and the inventory is resent (a wind charge also clears the
  cooldown the client predicted); `setShouldConsume(false)` keeps the item (a trident also keeps its durability). Differences
  from Paper: the throw sound of splash/lingering potions and experience bottles still plays for a cancelled launch (vanilla plays
  it before the spawn), and a snowball's stat is awarded after the item is used, as before.
- `PlayerItemCooldownEvent` (0435): every cooldown a player gets on the server (`ItemCooldowns#addCooldown` of
  `ServerItemCooldowns`), items of mods too. `PlayerShieldDisableEvent` (0917): only when an attacker disables the shield
  (`blockUsingShield`); a mod calling `disableShield()` itself gets vanilla's 100 ticks, as a null attacker does in Paper.
- `PlayerAttackEntityCooldownResetEvent` (0344): after the damage event of a player's hit; cancelled, the attack cooldown is kept.
  Not for the hit ignored during invulnerability (Paper does not fire it there either).
- `PlayerBedFailEnterEvent` (0517): may cancel, stop the explosion (beds outside the overworld) or change the message.
- `PlayerClientOptionsChangeEvent` (0340): before new options are applied; not for the options a player joins with (the
  `ServerPlayer` constructor applies them before it has a connection).
- `PlayerConnectionCloseEvent` (0270): after the listener handled the disconnect, for players in the game/configuration phase
  and logins past authentication (VERIFYING and later), synchronously.
- `PlayerHandshakeEvent` (0089): for login handshakes, before Spigot's BungeeCord parsing, which a handling listener replaces
  (spoofed UUID, profile, address, host); `setFailed` refuses the connection. Fired with Velocity forwarding on too, as in Paper.
- `PlayerInsertLecternBookEvent` (1063), `PlayerLecternPageChangeEvent` (0464, previous/next buttons), `PlayerLoomPatternSelectEvent`
  (0465; a pattern the loom does not offer is set with index -1), `CartographyItemEvent` (0935, taking the result; as in Paper the
  result slot no longer recomputes itself when it changes, so plugins can set it), `PlayerNameEntityEvent` (0496, name as
  Adventure, persistence switchable), `PlayerItemFrameChangeEvent` (0631: PLACE, ROTATE, REMOVE by a player).
- `PlayerSignCommandPreprocessEvent` (0560): for each `run_command` click event of a sign, logged like a typed command (when
  `spigot.yml` logs commands); may change the command or the player; cancelled, the sign's remaining commands do not run.
- `PlayerTeleportEndGatewayEvent` (0122): instead of `PlayerTeleportEvent` for a player going through an end gateway; the player is
  moved to the event's destination without a second teleport event. `PlayerPostRespawnEvent` (0282): after a respawn through
  vanilla's `PlayerList#respawn` (death, leaving the End).
- `PlayerRespawnEvent#getRespawnFlags` and `RespawnFlag` (0519): BED_SPAWN, ANCHOR_SPAWN from the event's own arguments,
  END_PORTAL when leaving the End; Paper's constructor with an `ImmutableSet.Builder` of flags goes to
  `FusionCoreEvents.newPlayerRespawnEvent`.
- `AsyncChatDecorateEvent` (0010): chat messages, on what NeoForge's chat decorator (`ServerChatEvent`) left, off the main thread
  (where NeoForge's decorator already runs). `AsyncChatCommandDecorateEvent`: chat arguments of commands (`/say`, `/me`, `/msg`),
  run on the chat executor and joined through the source's chat chain, as in Paper; with no listener the message resolves as in
  vanilla. A cancelled event keeps the message.
- `IllegalPacketEvent`: class only; Paper deprecated it and does not fire it either.

### Paper's server, profile and world events

- Profile events (0139, 0167): Paper subclasses authlib's session service and profile repository. Fusion Core wraps whatever
  `YggdrasilAuthenticationService` (or a mod) hands to `Services.create` (`ServicesMixin`, `FusionCoreProfileServices`), so every
  lookup through the server's services fires them: `PreLookupProfileEvent` (a listener giving the UUID skips the request) and
  `LookupProfileEvent` for name lookups (name cache, whitelist/op commands, mods); `PreFillProfileEvent` (textures given by a
  listener skip the request) and `FillProfileEvent` for profile fetches (skulls, `PlayerProfile#complete`). Vanilla fetches by
  UUID only: the name in the events comes from the server's name cache, empty when unknown (Paper passes a skull's name through
  its own cache key). Code that checks `sessionService() instanceof YggdrasilMinecraftSessionService` no longer matches.
- `ProfileWhitelistVerifyEvent` (0145): at login, after the ban check; the decision before the event is the player list's own
  (`isWhiteListed`, which the dedicated server overrides), the kick message comes from the event or `spigot.yml`. Other callers
  of `isWhiteListed` (enforce-whitelist kicks) do not fire it, as in Paper on a dedicated server.
- `WhitelistToggleEvent` (0279) before the whitelist flag changes; `WhitelistStateUpdateEvent` (0841) on every add to / remove
  from the whitelist, cancellable.
- Weather causes (0540): `WeatherChangeEvent.Cause`/`ThunderChangeEvent.Cause` as top-level `$Cause` enums; COMMAND
  (`ServerLevel#setWeatherParameters`), NATURAL (weather cycle), SLEEP (`resetWeatherCycle`), PLUGIN (`World#setStorm`/
  `setThundering`), UNKNOWN otherwise (mods, Spigot's constructor). Paper's constructors with a cause go to
  `FusionCoreEvents.newWeatherChangeEvent`/`newThunderChangeEvent`.
- `WorldGameRuleChangeEvent` (0460): `/gamerule` for boolean and integer rules (rules of other types, from mods, are set as
  before), and `World#setGameRule`/`setGameRuleValue` without a sender. Cancelled from the command, the rule's change callback
  still runs and the command reports the unchanged value, as in Paper.
- `StructuresLocateEvent` (0472): around `ChunkGenerator#findNearestMapStructure` (`/locate`, explorer maps, eyes of ender,
  dolphins, `World#locateNearestStructure`); a generator subclass overriding the method without calling it is not covered.
  Built only when someone listens.
- `GS4QueryEvent` (0281): the basic and full answers of the query protocol (`enable-query`) are built from the event's response.
  The full answer's plugin list is Paper's format (`<server> on <bukkit version>: name version; ...`), listed only when
  `settings.query-plugins` in `bukkit.yml` allows it.

### Paper's additions to Bukkit block events, shear drops

- `BlockCanBuildEvent#getHand` (0870): the hand of the placement (`BlockItem`, `StandingAndWallBlockItem`).
  `BlockDamageEvent#getBlockFace` (0864): the clicked face when a player starts breaking a block; null for events built with
  Spigot's constructor, as with Paper's deprecated one. `BlockMultiPlaceEvent` gets the placing hand (0319) from CraftBukkit's
  factory and from NeoForge's multi-place event. `BlockPhysicsEvent#getChangedBlockData` (API 0138).
- Paper's constructors of these events and of `SignChangeEvent` (component lines, legacy text inside: RGB kept, click/hover lost)
  go to `FusionCoreEvents.new<Event>`; the extra argument reaches the event through `FusionCoreEvents.takeConstructorArgument`.
- Shear drops (0916): `PlayerShearEntityEvent`/`BlockShearEntityEvent` `getDrops`/`setDrops` (`FusionCoreShearing`). On NeoForge both
  shears and dispensers go through `IShearable`; the event now fires once the entity is shearable (Arclight fired the player's
  before `isShearable`, also for sheep already sheared), with Paper's default drops of sheep, mooshrooms, snow golems and bogged
  computed beforehand as Paper does, and the drops the event ends with are what gets spawned: the entity's own drops are held back
  while it shears. Entities of mods get an empty list and keep their own drops, as in Paper. A dispenser shearing an entity did not
  fire `BlockShearEntityEvent` at all on NeoForge (Arclight had no hook there); it does now, cancellable.

### Lifecycle events and Brigadier commands for plugins

- Paper's lifecycle event API (patches 0441/0926): `LifecycleEventManager`, handler configurations with priority and monitor,
  `Plugin` as a `LifecycleEventOwner`, `Plugin#getLifecycleManager` (on `JavaPlugin`: handlers can be registered only during
  `onEnable`, as in Paper), `UnsafeValues#createPluginLifecycleEventManager`; a plugin's handlers go when it is disabled. The server
  classes are Paper's under Paper's names. Deviations: the event type provider and `MessageComponentSerializer.message()` are the
  core's objects directly, not found with `ServiceLoader` (which does not see the core from a plugin's loader); a handler that throws
  is logged and the other handlers still run (Paper throws, which stops the server start); `LifecycleEvents.TAGS` is left out until
  Paper's registry API is ported; the `plugin()` helpers of `LifecycleEvents` use raw types (javac does not see `Plugin` as a
  `LifecycleEventOwner`, the erased descriptors are Paper's).
- `LifecycleEvents.COMMANDS` and `Commands` (0965): fired for plugins once the POSTWORLD plugins are enabled (before the help map
  and the dispatcher are rebuilt) and again after every data pack reload (`/minecraft:reload`, cause `RELOAD`), as in Paper; the
  help map is rebuilt after a data pack reload too. Paper keeps all commands in the vanilla dispatcher; Spigot, and so Arclight,
  rebuilds the dispatcher from Bukkit's command map in `syncCommands`. So `PaperCommands` puts a registered command into the command
  map as a `PluginVanillaCommandWrapper` (a `PluginIdentifiableCommand`: `/help` lists it under its plugin, plugin managers find
  it) under Paper's labels and precedence (`plugin:command` and `command` always, each alias and `plugin:alias` unless another
  plugin holds it; vanilla and mod commands give way), and `syncCommands` copies the plugin's node itself into the dispatcher under
  each label (`BukkitCommandWrapperMixin`), so arguments, suggestions and the client's tree are Brigadier's. Aliases are copies of the
  node (Paper's `FLATTEN_ALIASES`), not redirects. Plugin commands of the previous round leave the command map before a reload round.
  `BasicCommand` as in Paper. `Commands#getDispatcher` is a mirror of the server's dispatcher, as in Paper: a node registered
  on it directly goes into the server's dispatcher and into the command map as a vanilla-style command under the plugin's prefix.
  Paper runs the handlers of bootstraps too; that comes with
  `paper-plugin.yml` support.
- Old Brigadier API (0276): `CommandRegisteredEvent` fires when `syncCommands` wraps a Bukkit command (only when someone listens):
  the listener may replace the node, cancel it, or make it a raw command (the server keeps the Bukkit node, clients get the plugin's;
  the link lives in `FusionCoreBrigadier`, and `CommandsMixin#sendCommands` swaps root children, since NeoForge builds the tree with
  its own `CommandHelper`). Paper 1.21.1 declares the event but no longer fires it; here Bukkit commands still go through that
  wrapper, so it fires as on Paper before 1.20.6. The vanilla command source is Paper's `BukkitBrigadierCommandSource`; the wrapper
  is Paper's `BukkitBrigadierCommand`; `PaperBrigadier` and `MessageComponentSerializer` convert Adventure components and Brigadier
  messages. `CommandSyntaxException` is not a `ComponentMessageThrowable` (Brigadier is a library the core does not transform).

### Brigadier argument types

- Paper's `ArgumentTypes` (patch 0965): every argument type wraps the vanilla one (`VanillaArgumentProviderImpl`), parses with it and
  converts the result to API objects (entity/player selectors and profiles as resolvers, block and fine positions, block states,
  item stacks and predicates, Adventure colors, components and styles, signed messages, scoreboard slots, keys, ranges, worlds,
  game modes, height maps, UUIDs, criteria, look anchors, time, structure mirror/rotation, registry resources and keys).
  `CustomArgumentType` (and `Converted`) as in Paper. `ApiMirrorRootNode` swaps such argument nodes for
  `WrappedArgumentCommandNode`s that carry the vanilla type (what clients get) and parse with the API type, for nodes registered
  through `Commands` and on the registrar's dispatcher; unknown argument types are refused at registration, as in Paper.
  Differences: a node whose tree holds no API argument type is kept as it is (Paper copies every node), the server's nodes are handed
  out as they are (the server's source is Paper's `CommandSourceStack`, no `ShadowBrigNode`), the wrapped/unwrapped links live in
  weak identity maps (Brigadier is a library the core does not transform), the provider is the core's object, not found with
  `ServiceLoader`. The command build context is taken where vanilla builds its commands (`ReloadableServerResourcesMixin`).
- `resource(RegistryKey)` looks the value up in Bukkit's registry for that key (`FusionCoreRegistries`: Bukkit's `Registry` constants,
  which Arclight fills with mod entries too); Paper looks it up in its own registries. `RegistryKey`/`TypedKey` are Paper's; the rest
  of Paper's registry API comes later.
- `SignedMessageResolver#resolveSignedMessage`: Paper adds a `MessageArgument#resolveChatMessage` overload taking the message; here
  `MessageArgumentMixin` lets the vanilla method read the message out of the resolver. The message is `FusionCoreSignedMessage`
  (Paper's `adventureView()`).

### Plugins built against Mojang's names

Paper's runtime has used Mojang's names since 1.20.5, and plugins built with paperweight say so in their manifest
(`paperweight-mappings-namespace: mojang` or `mojang+yarn`); Paper also takes a `paper-plugin.yml` jar without the attribute as
Mojang-mapped (patch 0020, `PluginRemapper`). Such plugins get `MojangClassLoaderRemapper` instead of Spigot's mappings: the
server runs on Mojang's names, so their Minecraft references stay as they are; CraftBukkit classes of the unversioned package
(Paper's) move to the core's versioned package when the core has them there. Reflection sees the server's real names. An unknown
namespace is treated as Spigot's (Paper refuses the plugin). Plugins with an embedded CommandAPI for Paper (Backuper) now run.

### Plugins with paper-plugin.yml

- A jar with `paper-plugin.yml` is described by it, also when it has a `plugin.yml` (as in Paper). The file is read into Bukkit's
  `PluginDescriptionFile`, so Bukkit's plugin manager orders, loads and enables Paper plugins together with Bukkit ones: a server
  dependency with `load: AFTER` becomes `loadbefore`, a required one `depend`, an optional one with `load: BEFORE` `softdepend`
  (Paper's defaults: required unless said otherwise, no ordering unless `load` says so); `defaultPerm`, permissions, `provides` as in
  Paper. The rest lives in `FusionCorePaperPlugins`.
- Loading runs the plugin's `PluginLoader` with a class of the plugin's own class loader; its libraries (`JarLibrary`,
  `MavenLibraryResolver`, which uses the Maven Resolver the installer ships, as Spigot's library loader does, and downloads into
  `libraries/` like Paper) form the plugin's library class loader. Then the `PluginBootstrap` runs (`bootstrap`, then `createPlugin`,
  which must return a class of the plugin's class loader). `LifecycleEvents.COMMANDS` fires for bootstrap owners first, then for plugins.
- Differences: the bootstrapper runs when the plugin is loaded (during server start, before worlds, after the registries are frozen),
  not before the server loads anything; in 1.21.1 a bootstrap can only register `COMMANDS` handlers and registry events, which the core
  does not have yet. Class lookup is Bukkit's (Arclight's), wider than Paper's isolation: `join-classpath: false` and
  `has-open-classloader` are not enforced, `dependencies.bootstrap` is ignored. Paper's internal plugin manager
  (`PaperPluginManagerImpl`) is not there: tools that look for it (PlugManX) use Bukkit's.

### Reading Paper's registries

- `RegistryAccess.registryAccess()` is the core's `FusionCoreRegistryAccess` (Paper finds its implementation through `ServiceLoader`,
  which cannot see the core). Every `RegistryKey` answers with Bukkit's `Registry` constant, so `getRegistry(RegistryKey.X) == Registry.X`;
  Arclight keeps those registries in step with vanilla, mod entries included. The deprecated `getRegistry(Class)` also answers for types
  that are simple (enum) registries here but server-backed in Paper (`Biome`, `Attribute`, `Particle`, `PotionType`, `Sound`, `Art`,
  `EntityType`, `Fluid`); `null` for types without a registry (`Material`).
- Tags: Craft registries read the vanilla registry's tags, as Paper's `CraftRegistry`; simple registries go through `UnsafeValues#getTag`
  as in Paper, and `getTag` there answers for every registry with a vanilla counterpart (Paper only for entity types and fluids, its other
  registries being server-backed). Mod tags are visible like vanilla ones. `NamedRegistryKeySetImpl`/`PaperRegistrySets` are Paper's
  server classes under Paper's names; `convertToNms` looks entries up in the running server's registries.
- `registry/keys/**` (generated keys) are copied as published; the `@MinecraftExperimental(TRADE_REBALANCE)` annotations on a few
  enchantment tag keys are comments, Spigot's `MinecraftExperimental.Requires` has no such constant.
- Feature flags: `requiredFeatures()`, `World#getFeatureFlags` and datapack flags return the canonical `FeatureFlag` objects
  (`FeatureFlag.VANILLA` …) as Paper does. Spigot built new `CraftFeatureFlag`s without `equals`, so
  `world.getFeatureFlags().contains(FeatureFlag.VANILLA)` was false. `WorldInfo#getFeatureFlags` of a world being created is the server's
  feature set (vanilla has one for all worlds).

### Early bootstrap and registry modification

- Paper plugins with a bootstrapper are bootstrapped when the world's data starts loading (`WorldLoader#load`, before the data
  registries), as Paper does before it starts loading: `FusionCorePaperPlugins#enterBootstrappers` creates their class loaders (Bukkit's
  `PluginClassLoader`, not yet attached to a `JavaPluginLoader`), runs their `PluginLoader` and `PluginBootstrap` there, and Bukkit's
  loader later takes the same class loader over; `createPlugin` runs then. Differences from Paper: this happens after mods loaded;
  bootstrappers run in the order of the jar names, not of `dependencies.bootstrap`; a bootstrapper sees only its own jar and libraries.
  Plugin classes loaded at that point go through Commodore with the compatibilities of `bukkit.yml`, as `CraftServer` will set them.
  A plugin not in the plugins folder at startup, or with an update waiting in the update folder, is bootstrapped when it is loaded; registry handlers it registers then are reported in the log instead of silently never running. A bootstrap that fails at
  startup is logged and the plugin is not loaded, as in Paper; other plugins and the server go on.
- `RegistryEvents.ENCHANTMENT`: `entryAdd` runs for every enchantment loaded from data packs (mods' included) and may change it;
  `freeze` runs before the registry is frozen and may add enchantments, as in Paper (`RegistryDataLoader`, patches 1014/1015). Changed
  and added entries lose their known-pack origin, so clients receive them with the registry data. Paper's `Conversions` converts
  components through its own serializer; here through vanilla's component codec and Adventure's JSON against the same lookup.
- `RegistryEvents.GAME_EVENT` (built-in registry): NeoForge fills and freezes built-in registries while mods load, before any plugin
  can be bootstrapped. `entryAdd` runs right after the bootstrappers for every registered game event (mods' too) and changes the
  radius on the registered `GameEvent` itself (the registry, holders and vanilla's constants keep the same object, as with Paper);
  `freeze` is fired, but registering a new game event from it throws `UnsupportedOperationException`: on NeoForge only mods add
  built-in entries.
- `RegistryEventTypeProvider` is the core's implementation directly (Paper uses `ServiceLoader`).
- `FusionCoreRegistries` reads Bukkit's registries when asked: `org.bukkit.Registry` needs the server, which does not exist yet during
  a bootstrap.

### Event additions that plugins already call

- `PrepareAnvilEvent` and `PrepareSmithingEvent` extend Paper's `PrepareResultEvent`, and `PrepareGrindstoneEvent` extends Paper's
  deprecated `com.destroystokyo…PrepareGrindstoneEvent`, as in Paper. A mixin cannot change a superclass: `SuperclassProcessor` (a
  post-processor of `ArclightMixinPlugin`) rewrites the class and its `super` calls when the (empty) mixin on it applies. As in Paper,
  these three events are fired after the result is computed (`FusionCorePrepareResult`, Paper's `callPrepareResultEvent`), not from
  inside `createResult`; mods that hook `createResult` see vanilla's result being set. Skript 2.15.4 failed to enable on the old
  hierarchy (`LambdaConversionException`).
- `PlayerDeathEvent#getPlayer`, `PlayerInteractEvent#getInteractionPoint`.
- Critical damage (Paper patch 0588): a critical player hit (NeoForge's `CriticalHitEvent` decides, the hook is in
  `fusioncore-neoforge`) and a critical arrow mark the damage source; `EntityDamageByEntityEvent#isCritical`; sweep damage shares the
  attack's source, so it is critical with the hit, as Paper marks it.
- `EntityUnleashEvent` is cancellable and has `dropLeash` (Paper patch 0481): every unleash the core fires passes vanilla's drop
  flag and uses the event's; distance, riding and a sitting pet pulled too far can be cancelled, as in Paper; the dimension-change
  event only fires for a leashed entity. Spigot's constructors leave `dropLeash` false, as Paper's do.

### Fusion Core's own names

- The server's files carry Fusion Core's name: `fusioncore.conf` (was `arclight.conf`) and the working folder `.fusioncore/` (was
  `.arclight/`: the unpacked core and the plugin class cache). A server that ran an earlier build has them renamed on the first start.
- Log lines, thread names, the console's name, crash report sections, the launch target (`fusioncoreserver`), the mixin configurations
  (`mixins.fusioncore.*.json`, as they appear in stack traces) and the log4j configuration say Fusion Core. The start line is
  `Fusion Core <version> | Bukkit/Spigot/Paper on NEOFORGE`, `/version` answers `Fusion Core <version>.`, the server's brand is
  `Fusion Core neoforge`. The Arclight origin is stated in `/fusioncore`, the config header, README and LICENSE (GPL-3.0).
- Kept on purpose: the mod id `arclight` (other mods detect the Bukkit hybrid by it and switch their compatibility on; the displayed
  name is Fusion Core), the permission handler id `arclight:permission` (written into NeoForge's config), the plugin channel
  `arclight:custom/bukkit`, the `arclight.*` system properties and the Java packages `io.izzel.arclight`.
- After the loader is installed, the files its installer leaves and Fusion Core does not use are removed: `run.bat`/`run.sh` and
  `user_jvm_args.txt` while unedited, the installer's `installer.log` and jar.

### Direct access to Minecraft fields that Spigot makes public

Spigot's access transformers make many Minecraft fields public, and plugins compiled against Spigot read or write them directly.
Here Minecraft keeps vanilla access (widening some fields breaks mods' mixins), so such code threw `IllegalAccessError` (dynmap on
`Biome#climateSettings`). After remapping, a plugin's field instruction on a Minecraft class whose field is not public becomes an
`invokedynamic` bound once to a getter or setter of that field (`FusionCoreNmsAccess`); public fields are left alone.

### Machines of mods act as their owner 

Protection plugins see only what Bukkit events show them. A mod's machine that breaks or places blocks with no player (Create's
drill, saw, harvester, plough, contraption assembly; any mod changing the world from a block entity's or entity's tick) fired no
event at all, so it could reach into claims. Now:

- The player who places a mod's block entity (a block entity type outside `minecraft:`) is its owner: UUID and name are stored in
  the block entity's NeoForge persistent data under `fusioncore:owner`. A mod's fake player placing it (Create's deployer, which acts
  as its own owner) counts as that player. What a machine creates while it runs (block entities it places, entities it spawns, such
  as a contraption from a bearing) inherits the owner.
- While a mod's block entity or entity with an owner ticks, its changes at other positions through `Level#destroyBlock` (before the
  drops) and `Level#setBlock` fire `BlockBreakEvent` (a block removed or replaced by another block) or `BlockPlaceEvent` (a block put
  where there was air, a fluid or a replaceable block) as a fake player with the owner's UUID and name; cancelled, the change does not
  happen. State changes of the same block (its properties) fire nothing. Vanilla block entities are untouched (they have their own
  events: pistons, dispensers).
- Create: `BlockHelper#destroyBlockAs` without a player is checked at its start, before the drops are handed out (checking at
  `setBlock` would duplicate them). A contraption is assembled all or nothing: a denied block fails the assembly with Create's own
  "unmovable block" message before anything is taken from the world. On disassembly a block that may not be placed drops as an item,
  as Create does when the spot is taken.
- `fusioncore.conf`, section `machines`: `owner-events` (default `true`); `unknown-owner` for machines with no owner (placed before this
  version, or by commands): `allow` (default, no events, as before), `deny` (they change nothing outside their own position), `mod`
  (events as the fake player `[<modid>]` with a fixed UUID, which claims can trust); `ignored-mods` (mod ids left alone).

- Known limit: login plugins such as AuthMe cancel every action of a player who has not logged in, and the owner's fake player
  never logs in, so there a machine works only while its owner is online and logged in (Create's deployer behaves the same way).

### Paper's world names on new servers

- A new server (first start with no `fusioncore.conf` and no world folder named by `level-name`) writes `compat.symlink-world = true`,
  so the vanilla nether and end are `world_nether` and `world_the_end`, as on Paper and CraftBukkit, and plugins and configs written
  for Paper find them. Existing servers keep what their `fusioncore.conf` says (default `false`, the folder names `world/DIM-1`,
  `world/DIM1`), because plugin data are stored under those names. Mod dimensions on a new server are named `world_<namespace>_<path>`.
- On Windows, where a symbolic link needs developer mode or administrator rights, the `world_nether` folder is made as a directory
  junction (`mklink /J`, no rights needed); a junction made on an earlier start is recognised and not reported as a conflicting file.

### Difficulty of the vanilla nether and end

- with `compat.symlink-world`, a link that cannot be made (Windows without developer mode or administrator rights) is one WARN,
  not an ERROR with a stack trace; the world names work without it.

- `World#setDifficulty` on the vanilla nether or end did nothing: their data derive from the overworld's (`DerivedLevelData` ignores
  writes). It now sets the server's difficulty, as `/difficulty` does, which all worlds sharing that data use. Worlds created through
  `WorldCreator` have their own data and their own difficulty, as before. Deviation from Paper: there, each world has its own difficulty.

### Unversioned CraftBukkit names, no illegal-access warning

- A plugin's `Class.forName`/`ClassLoader#loadClass` of `org.bukkit.craftbukkit.X` (Paper's package since 1.20.5) finds the core's class in
  the versioned package when the unversioned one does not exist (Multiverse-Core's `CraftSpawnCategory`).
- No "Loaded class … which is not a depend or softdepend of this plugin" warning: Paper removed it ("we are never going to enforce it");
  nothing was enforced, it only filled logs (PlasmoVoice's lavaplayer addon, several popular plugins).

### Tick time rings and CraftBukkit's dimension names

- Paper's `MinecraftServer#tickTimes5s`/`tickTimes10s`/`tickTimes60s` (patch "Add tick times API and /mspt command"), filled at the end
  of each tick from vanilla's own measurement. Their type is `FusionCoreTickTimes` with Paper's members; a class cannot be added to
  Minecraft's package here, so a plugin's reflective lookup of `net.minecraft.server.MinecraftServer$TickTimes` is answered with it
  (`ArclightReflectionHandler`). TabTPS enables on its Paper path again.
- CraftBukkit names the vanilla nether and end `<level>_nether` and `<level>_the_end`; on the mod loader they live in the level folder and
  are named by it (`world/DIM-1`, `world/DIM1`, as in Arclight). The names stay, and `getWorld` with CraftBukkit's names finds the same
  worlds. Multiverse-Core 5 still fails from the second start: it matches worlds by `getName()`.
- Not the core: ImageFrame scans modules with ClassGraph, and NeoForge's module readers do not list their contents.

### Corpus of popular plugins, first whole run

- Paper's channel initialization listeners (server patch 0521): `io.papermc.paper.network.ChannelInitializeListener`,
  `ChannelInitializeListenerHolder` (called at the end of the server channel initializer) and `ConnectionEvent` (fired into the
  pipeline when compression is set or turned off). ViaVersion uses them on Paper; without them it fell back to reflecting into the
  server connection and failed to inject.
- Plugin class lookup across plugins asks the plugin's (transitive) dependencies first, then every plugin, as Paper's dependency
  groups do: a class that two plugins bundle unrelocated came from whichever loaded first (Multiverse-Inventories bundles ViaVersion's
  NBT library, and ViaBackwards got its `CompoundTag` instead of ViaVersion's: `LinkageError`).
- Plugins built against Mojang's names get plain `jar:` URLs for class resources, as on Paper, instead of Arclight's
  `remap:` URLs (FAWE checks that its manifest URL starts with `jar`); their class bytes need no remapping.
- `Class#getName` as rewritten for plugins keeps the slash of a hidden class (`Outer$$Lambda/0x…`): Arclight turned it into a dot,
  which moved the class into a package `Outer$$Lambda` for code that derives the package from the name (floodgate's lmbda).
- Paper's statics on Bukkit classes (`Bukkit.motd()`, `ItemStack.deserializeBytes`, …) are also real public statics of those classes
  (`StaticHolderProcessor`, delegating to the holders): plugins that look them up by reflection found nothing (Geyser, `motd`).
  Plugin bytecode still calls the holders through `PaperCallTransformer`.
- Paper's marker classes `com.destroystokyo.paper.PaperConfig` and `PaperWorldConfig` (patch 0005), kept by Paper so that older plugins
  and PaperLib detect Paper (squaremap; FAWE's PaperLib takes its Paper path and starts). Plugins that then reach into Paper's server
  internals can fail where their Spigot path worked (TabTPS needs `MinecraftServer$TickTimes`).
- Not done: `Biome#climateSettings` stays private (Spigot's access transformer makes it public; widened, a mixin on `Biome` fails and
  the server does not start), so dynmap's Spigot helper still fails.
- Adventure's `net.kyori:option` shipped as 1.1.0 instead of 1.0.0: a strict superset of what Adventure 4.17 needs. Plugins that
  bundle 1.1.0 unrelocated (DecentHolograms) otherwise mixed classes of both versions in one package (`IllegalAccessError`).
  Deviation: Paper 1.21.1 ships 1.0.0.

### Version history

- Paper's `com.destroystokyo.paper.VersionHistoryManager` (patch 0033): `version_history.json` in the server folder keeps the current
  and the previous version, loaded before the plugins; `/version` shows the previous version. Plugins detect Paper by its
  `VersionData` class: MythicMobs now turns on its Paper functionality (and, on 1.21.1, hits its own stop-time NPE, as on Paper).

### Paper's plugin loader internals

- `PluginClassLoader` and `LibraryLoader` are public, as in Paper (Spigot has them package-private, so a plugin built against Paper
  that casts its class loader failed with `IllegalAccessError`). `PublicClassProcessor` changes the access when Mixin applies the
  class; `fusioncore-common` compiles against public stubs of them in `src/stubs` (never packaged).
- `PluginClassLoader` is a `ConfiguredPluginClassLoader` (configuration, plugin, `init`, 4-argument `loadClass`, group) with a public
  `dependencyContext`. `PaperClassLoaderStorage` has Paper's groups (global, Spigot view with libraries only of dependencies, open,
  access-backed). Deviation: Bukkit plugins still find each other's classes through `JavaPluginLoader`, as in Spigot and Arclight;
  class loaders that other plugins put into the global group (`registerUnsafePlugin`, open groups) are searched after it.
- `PluginManager#isTransitiveDependency` (depend, softdepend and provides, over the loaded plugins and Spigot's own graph) and
  `overridePermissionManager` (every permission method goes to the given manager; null goes back, where Paper would fail).
  `SimplePluginManager#loadPlugins(File, List)`: the extra jars are sorted by dependencies with the directory's.
- plugin.yml `paper-plugin-loader`: the `PluginLoader` runs before the plugin is created and its libraries join the ones of
  `libraries` in one class loader; `paper-skip-libraries: true` drops `libraries`. `PluginDescriptionFile#getPaperPluginLoader` and
  Paper's all-fields constructor.
- `LibraryLoader.LIBRARY_LOADER_FACTORY`/`REMAPPER` (applied to every plugin's libraries when set) and `createLoader(desc, paths)`.
- `JavaPlugin#init(Server, …, PluginMeta, Logger)`: keeps Bukkit's loader of the class loader; Spigot's logger field only takes a
  `PluginLogger`, so another logger passed there is replaced by the plugin's own.
- `/reload` as in Paper: `/reload confirm` (or `-DLetMeReload`), `/reload permissions`, `/reload commands`; `Server#reload` throws
  `ReloadCommand.RELOADING_DISABLED_MESSAGE` once a lifecycle event handler forbids reloading (Paper patch 0926).
- `SimpleCommandMap(Server, Map)`, `PaperPluginLogger.getLogger(PluginMeta)`, `UnsafeValues#getTimingsServerName` (Paper's default
  "Unknown Server") and `reportTimings` (says timings are unavailable), `CommandBlockHolder` on command blocks (the state's snapshot)
  and command block minecarts (last output, success count), `@DoNotUse`.

### New block data in EntityChangeBlockEvent

- Paper patch 0683: where an entity removes a block, `EntityChangeBlockEvent#getTo` is the fluid left behind (water for a waterlogged
  block) instead of air: wither, ravager, enderman taking a block, rabbit eating the last carrot stage, silverfish leaving an infested
  block (the host stone when mob griefing is off), splash potion putting out fire, projectiles breaking chorus flowers, dripstone
  and TNT, boats breaking lily pads.
- Sheep: eating tall grass fired no event at all in Arclight; it now fires with the new block. The grass block it eats is reported as
  dirt, not air. A cancelled event keeps the block but the sheep still eats (regrows wool), as in Paper; Arclight skipped that.
  Unlike Paper, the event is not fired pre-cancelled when mob griefing is off (NeoForge's `canEntityGrief` decides first).

### Inventory, server and vehicle event additions

- `FurnaceBurnEvent#setConsumeFuel(false)` (Paper 0257/0511): the furnace burns without using up the fuel item; a lava bucket stays a
  lava bucket (NeoForge's crafting-remainder branch is skipped as well).
- `InventoryOpenEvent#titleOverride` (0417/0874): the title in the open-screen packet of `ServerPlayer#openMenu` and of inventories
  plugins open through `HumanEntity#openInventory`; the view keeps its own title, as in Paper.
- `EnchantItemEvent#setItem` (0453/0946): the item set is written into the stack in the table, which is then enchanted.
- `TabCompleteEvent#isCommand`/`getLocation` and Paper's constructor (0073/0161); the list of completions is copied so it is mutable.
  `CraftServer#tabComplete` passes the command flag and the position.
- `PaperServerListPingEvent`'s component constructor (the MOTD is kept as legacy text); `StatusClient#isLegacy` is always false: pings of
  pre-1.7 clients do not fire the event here.
- `VehicleBlockCollisionEvent#getVelocity`: the movement before the collision (0315/0786). The handler list moves up to
  `VehicleCollisionEvent`, as in Paper, so one listener gets block and entity collisions. Not done: Paper's protected
  `ServerListPingEvent` constructors with a component, which only subclasses call.
- Fixed Arclight's typo in the vehicle block collision (`vec3d.x < vec3d.x`): a collision on the west side reported the block the
  vehicle was in instead of the one to the west.

### Player event additions

- `PlayerMoveEvent#hasChangedPosition`/`hasChangedBlock`/`hasChangedOrientation` and the explicit variants (Paper API 0263).
- `PlayerItemConsumeEvent#setReplacement`: the item left in the hand after eating or drinking (0030/0073).
- `PlayerItemDamageEvent#getOriginalDamage`: the damage before Unbreaking (0344/0691).
- `PlayerItemMendEvent#getConsumedExperience` (0393/0815), for orbs and for `Player#applyMending`; `get/setDurabilityToXpOperation`
  throw, as in Paper (enchantment effects compute it since 1.21).
- `PlayerExpChangeEvent#getSource`: the orb (0043/0112). The amount is what is left after mending, as in Spigot and Arclight; Paper
  1.21.1 passes the orb's full value there.
- `PlayerPickupItemEvent#setFlyAtPlayer` (0058/0133): the pickup animation plays only when it is set; cancelling the event turns it off
  and uncancelling on, as in Paper. With a cancelled event and the flag set, the item flies to the player and stays on the ground.
- `PlayerResourcePackStatusEvent#getHash` returns null, as in Paper.

### Entity event additions

- `EntityPortalEnterEvent` is cancellable and has the portal type (Paper patches 0382/0781): nether and end portals fire it with
  `NETHER`/`ENDER` and let nothing happen when it is cancelled; end gateways fire it too, with `END_GATEWAY`, every tick an entity is
  inside, as in Paper. `EntityPortalEvent#getPortalType` comes from the teleport cause. The core now also passes Spigot's creation
  radius and `canCreatePortal` to `EntityPortalEvent` (Arclight used the 4-argument constructor, which gave the default radius).
- `EntityRegainHealthEvent#isFastRegen` (0091): the saturation heal at a full food bar.
- `EntityDismountEvent`/`VehicleExitEvent#isCancellable` (0155): a non-cancellable event ignores `setCancelled(true)`. The server fires
  them cancellable, as Paper 1.21.1 does; only Paper's constructor makes one that is not.
- `EntityShootBowEvent#setConsumeArrow`/`getConsumeArrow`/`getArrowItem` are Paper's deprecated names for Spigot's consumable methods;
  Paper's constructors (0108).
- `EntityBreakDoorEvent` carries the block left where the door was: the fluid of a waterlogged door, not always air (0343 and the door
  part of 0683).

### Cancellable death events

Paper's "Improve death events" (patch 0242), `PlayerDeathEvent#getItemsToKeep` (0284), `#shouldDropExperience` (0301) and Paper's
`PlayerDeathEvent` constructors.

- `EntityDeathEvent` is cancellable and carries `reviveHealth` (the max health by default) and the death sound. When a plugin cancels
  it, the entity stays alive with that health: a mob keeps the equipment it would have dropped, a donkey/mule/llama its chest, a
  player the inventory it had; nothing drops, no experience, no kill score, no `ENTITY_DIE` game event, no death animation. A
  cancelled armor stand is not removed (items it popped before the event are gone, as in Paper).
- The death sound is played from the event, not from `LivingEntity#hurt` (`shouldPlayDeathSound` is false when `hurt` would have been
  silent, and for silent entities). Paper plays it for every death that fires the event, so deaths that do not come from `hurt` now
  make a sound too. A mod's death sound with no Bukkit `Sound` shows as `null` in the event and still plays its own `SoundEvent`
  unless a plugin sets another sound.
- Arclight fires the death event from NeoForge's `LivingDropsEvent`, inside `dropAllDeathLoot`, so the cancellation is applied after it
  (`LivingEntityDeathMixin`, `ServerPlayerMixin`). Unlike Paper, NeoForge's `LivingDeathEvent`, waking up, the named-entity log line and
  the killer's `killedEntity` run before the event, in vanilla's order. A mod that already took the drops in `LivingDropsEvent` (a grave
  mod) does not give them back when a plugin cancels the death.
- Paper queues the player's health packet during `hurt` so a revived player never sees 0; here the packet is only sent from `doTick`,
  after the revive, so there is nothing to queue.
- `getItemsToKeep`: kept items go back into their slots (free slots for items the player did not have; Curse of Vanishing is never
  kept). Paper keeps the same player object on respawn; NeoForge builds a new one, so the kept slots are copied to it in `restoreFrom`.
  A player who logs out dead and respawns after joining again loses them.

### Tag lifecycle events

- `LifecycleEvents.TAGS` (`preFlatten`/`postFlatten` per API registry) as in Paper (patch 1053): fired from `TagLoader#build` for every
  registry that has an API key (mods' tags are in the registrars like vanilla ones), at startup and on each data pack reload;
  handlers are registered in a bootstrap, as in Paper. Function tags fire nothing, as in Paper.
- The cause is `INITIAL` for the first tag load of the process and `RELOAD` afterwards; Paper tells them apart by the executor the
  reload is applied on. Paper passes the event config to `TagLoader#build` as a parameter; here it is set on the loader `TagManager`
  creates. The fields of vanilla's `TagEntry` are widened for the registrars (Paper's access transformer does the same).

### Smaller API

`Location` (center/block/highest location, block key, explosions, nearby entities), `World` nearby-entity API,
explosions with a source entity (the source is not excluded from damage: vanilla has no switch for it),
`getChunkAtAsync` with the deprecated `ChunkLoadCallback`, `spawnEntity` with a spawn reason, per-world view and
simulation distance; `Block#breakNaturally(effect, exp)`, sound group, `randomTick`; `getState(boolean)`,
`Inventory/DoubleChest` `getHolder(boolean)` (always snapshots: Spigot has no live states); `Event#callEvent`;
`ItemStack` bytes (compressed NBT + data version, older data updated with vanilla's DataFixer), `of`, `empty`,
`isEmpty`, persistent-data view; `PersistentDataContainer` bytes; `Registry#getKey(OrThrow)`; BungeeCord
messages on `CommandSender`; `Entity`/`LivingEntity` accessors; `Server.Spigot#getPaperConfig()` — built from
Fusion Core's settings with the keys plugins read (`proxies.velocity.*`, `proxies.bungee-cord.online-mode`), since
there is no paper-global.yml; `UnsafeValues` item bytes, protocol version, biome key. Copied pure Paper API:
`MaterialSetTag`/`MaterialTags`, `io.papermc.paper.tag.*` (their exact-size check is off: mods add materials),
`ParticleBuilder`, `ClientOption`, `SkinParts`, `MessageCommandSender`, and a timings API that behaves as
Paper's does with timings disabled (everything no-op; use spark).

### PlayerProfile

Paper's `com.destroystokyo.paper.profile.CraftPlayerProfile` (patches 0136, 0165, 0175/0087, 0176), next to
Spigot's, as in Paper: `Server/Bukkit#createProfile(…)`/`createProfileExact`, `OfflinePlayer`/`Player`
`getPlayerProfile()` with Paper's return type (a call rewrite, the return types clash), `Player#setPlayerProfile`
(unlist from every viewer, re-add with the new profile, respawn in place, as Paper), `Skull`/`SkullMeta`
`get/setPlayerProfile`, and `AsyncPlayerPreLoginEvent` with the profile, raw address and hostname: the profile
left in the event, completed with textures, is the one the player logs in with (SkinsRestorer then uses its Paper
join listener). The event now also gets Spigot's `transferred` flag, which Arclight dropped. Deviations:

- The profile's textures are Spigot's `CraftPlayerTextures` over the Paper profile's own property map (Paper does it
  through `SharedPlayerProfile`); a changed texture is written back when it is read or passed to `setTextures`,
  as in Paper.
- Paper's `proxies.*.online-mode` settings do not exist: `complete()` treats BungeeCord and Velocity players as
  online-mode players, like Paper's defaults.
- Paper's profile class is registered for the `PlayerProfile` config alias, as in Paper, so Spigot code that only
  took Spigot's class (skull `setOwnerProfile`, the profile ban list) converts Paper's profile first.
- Arclight never set Spigot's `ServerPlayer.sentListPacket`; it is now set on join as Spigot does. Besides
  `setPlayerProfile`, this makes `Player#hidePlayer` take the hidden player off the viewer's tab list again.

Tab list on join: CraftBukkit's loop in `placeNewPlayer` is back — the new player gets only the players it
can see and its own entry goes only to players that can see it. Arclight kept vanilla's send-to-everyone, so a
player hidden in `PlayerJoinEvent` (vanish plugins) was listed in everyone's tab list.

### Paper events fired by the core

Each is fired where Paper fires it: `PlayerAttemptPickupItemEvent` (before Bukkit's pickup events, with
`flyAtPlayer`), `PreSpawnerSpawnEvent` (once a spawner attempt passed the spawn rules; a cancelled attempt still
resets the spawner delay, `shouldAbortSpawn` ends the tick's remaining attempts), `PlayerArmorChangeEvent` (the
per-tick equipment check), `PlayerChunkLoadEvent`/`PlayerChunkUnloadEvent` (after the chunk packet / the forget
packet; built only when a plugin listens) and `PlayerInventorySlotChangeEvent` (the player's own container listener
gets the old item too; a listener can turn off the advancement trigger, as in Paper).

`InventoryCloseEvent.Reason`: every close the core makes says why — `PLAYER` (the close packet), `PLUGIN`
(`closeInventory()`), `OPEN_NEW`, `TELEPORT`, `CANT_USE` (container no longer valid), `UNLOADED`, `DISCONNECT`,
`DEATH`; plus `HumanEntity#closeInventory(Reason)` and Paper's constructor. The outermost caller's reason wins (a
plugin closing inside a disconnect is still `DISCONNECT`), which is what Paper gets by passing the reason down.

`PlayerTradeEvent` / `PlayerPurchaseEvent`: fired when a trade is taken from a villager or a Bukkit custom
merchant (mods' own merchants have no event, as in Paper). Cancelled, nothing is paid and nothing is received; the
event can change the trade and turn off the uses increase and the experience. Paper restructures
`MerchantMenu#quickMoveStack` so a shift-click moves the result only after the event; here the shift-click fires the
event before vanilla moves anything and the result slot reuses it, so a cancelled shift-click cannot duplicate the
result either. Villagers' `notifyTrade` keeps vanilla behaviour outside a trade event (mods that call it directly).

Second batch, again where Paper fires them: `EntityJumpEvent` (mobs, pandas, ravagers; a cancelled jump
still starts the mob's jump delay), `PlayerJumpEvent` (cancelled: the player is put back without a teleport event),
`EntityMoveEvent` (non-player living entities that moved this tick), `PlayerStopUsingItemEvent`,
`PlayerUseUnknownEntityEvent` (client-side entities such as packet NPCs), Paper's `PlayerRecipeBookClickEvent`
(before Spigot's; Spigot's shift-click result is now used too, Arclight dropped it), `PlayerPickItemEvent`,
Paper's deprecated `TNTPrimeEvent` (after Spigot's, for the causes Paper fires it for), `BlockPreDispenseEvent`
(dispensers and droppers), `BeaconActivatedEvent`/`BeaconDeactivatedEvent`, `PlayerChangeBeaconEffectEvent`,
`PlayerFlowerPotManipulateEvent`, `UnknownCommandEvent` (Bukkit's `dispatchCommand` and vanilla's parser; Paper's
message is vanilla's two failure lines), `ServerResourcesReloadedEvent` (`COMMAND` for `/reload`, otherwise
`PLUGIN`), `EntityZapEvent`, `SkeletonHorseTrapEvent` and `PlayerElytraBoostEvent`.

Knockback and spawn point: Paper's `EntityKnockbackEvent` / `EntityKnockbackByEntityEvent` fire after Spigot's
knockback event, starting from its result (Paper's knockback is the change of velocity, written back into Spigot's
event); `EntityPushedByEntityAttackEvent` covers pushes caused by an attacker (arrows, sweeps on non-living entities,
the warden's sonic boom, the dragon's wings, ravager roars, hoglin and zoglin throws). The shield-block knockback now
carries its cause and the blocking entity on the entity that is knocked back (Arclight set them on the blocker, so the
event said `UNKNOWN`). `PlayerSetSpawnEvent` fires after Spigot's `PlayerSpawnChangeEvent` and can change or drop the
"Respawn point set" message; a cancelled spawn change is stopped explicitly. Unlike Paper, `setRespawnPosition` still
returns nothing, so `/spawnpoint` counts players whose change a plugin cancelled.

`ServerExceptionEvent` with Paper's exception types: a listener that threw (`ServerEventException`, never
for `ServerExceptionEvent` itself), a plugin that failed to enable or disable, a command or tab completer that threw
(`ServerCommandException`, `ServerTabCompleteException`) and a synchronous task that threw
(`ServerSchedulerException`). Paper's internal reports (chunk and region-file errors) are not wired: those code
paths differ on NeoForge.

`EntityInsideBlockEvent`: Paper adds it to every vanilla block that acts on entities inside it
(`entityInside`); here it fires where entities check the blocks they are in, for any block whose class overrides
`entityInside`, so mods' blocks get it too. Cancelled, the block does nothing to the entity.

Mob spawn range: Spigot's `mob-spawn-range` (`spigot.yml`, default 6 chunks) now applies, as on every
Spigot/Paper server; Arclight ignored it and spawned within vanilla's 8 chunks (128 blocks). Paper's
`PlayerNaturallySpawnCreaturesEvent` fires for each player before natural spawning with the radius
min(mob-spawn-range, view-distance, 8); a chunk spawns mobs only near a non-spectator player whose event was not
cancelled. Chunk ticking keeps vanilla's range, as in Spigot.

Third batch, where Paper fires them: `ServerTickStartEvent`/`ServerTickEndEvent` (the end event's time remaining
comes from vanilla's schedule, so `/tick rate` and sprinting count), `EntityAddToWorldEvent`/`EntityRemoveFromWorldEvent`
(the latter before the entity is marked invalid), `CreeperIgniteEvent` (with Paper's `Creeper#setIgnited`; a creeper
loaded ignited from disk does not fire it), `ExperienceOrbMergeEvent` (vanilla's orb merging and Spigot's merge on spawn,
where, as in Paper, only orbs of the same count merge), `PlayerPickupExperienceEvent`, `BlockDestroyEvent`,
Paper's world border events (borders of worlds only; Arclight keeps vanilla's link from the overworld's border to the
other dimensions', so one change fires for each of them), `PlayerAdvancementCriterionGrantEvent` (a cancelled grant is
taken back), `EndermanAttackPlayerEvent`, `AnvilDamagedEvent`, Paper's deprecated `BellRingEvent` (fired in place of
Spigot's, which it extends), `BeaconEffectEvent` and `PlayerDeepSleepEvent`. Fixes that came with it: beacon effects carry
Spigot's `EntityPotionEffectEvent` cause `BEACON` (Arclight left it `UNKNOWN`), and Mending no longer overwrites an
orb's value with the leftover (CraftBukkit's bug, fixed in Paper), which lost experience from stacked orbs.

Fourth batch, player events: `PlayerArmSwingEvent` (fired in place of Spigot's `PlayerAnimationEvent`, which it
extends), `PlayerOpenSignEvent` (before Spigot's `PlayerSignOpenEvent`, which then fires only when someone listens),
`PlayerStonecutterRecipeSelectEvent` (cancel, or pick another recipe of the list), `PlayerFailMoveEvent` (moved too
quickly, moved wrongly, clipped into a block; Spigot has no "moved into unloaded chunk" check, so that reason never
comes), `PrePlayerAttackEntityEvent` (the target's `skipAttackInteraction`, which has side effects, is still asked only
once), `PlayerReadyArrowEvent`, `PlayerStartSpectatingEntityEvent`/`PlayerStopSpectatingEntityEvent`,
`PlayerTrackEntityEvent`/`PlayerUntrackEntityEvent`, Paper's deprecated `ProjectileCollideEvent` (its cancellation
carries over to `ProjectileHitEvent`) and `PlayerQuitEvent.QuitReason` (`KICKED` after a kick that went through,
`TIMED_OUT`/`ERRONEOUS_STATE` from the connection, `DISCONNECTED` otherwise; the first reason sticks).

Pathfinding: Paper's `EntityPathfindEvent` offers each target a mob would path to (with the target entity when
the path is to an entity, through vanilla's navigations; mods' navigations that override `createPath(Entity, int)`
give no target entity); refused targets are left out. The slime events (`SlimeSwimEvent`, `SlimeWanderEvent`,
`SlimeTargetLivingEntityEvent`, `SlimeChangeDirectionEvent`) and `Slime#canWander`/`setWander` (saved as
`Paper.canWander`, like Paper) come with Paper's changes to the slime goals: a dead target is dropped, and the attack
goal forgets its target when it stops.

Teleport flags (Paper patch 0721 "More Teleport API"): `TeleportFlag` (`EntityState.RETAIN_PASSENGERS`,
`RETAIN_VEHICLE`, `RETAIN_OPEN_INVENTORY`, `Relative.X/Y/Z/YAW/PITCH`), `Entity#teleport(Location[, TeleportCause],
TeleportFlag...)` and `teleportAsync` with flags, `PlayerTeleportEvent#getRelativeTeleportationFlags` (and Paper's
constructor with them), `Player#lookAt`, and `Player#setRotation`, which Spigot refuses for players. The flags reach
Spigot's `teleport` of the Craft classes and act where Paper's patch acts: a vehicle may keep its passengers, a rider
its vehicle (both refused across worlds), a player keeps an open menu, and relative axes keep the client's velocity.
Teleports of the server itself (vanilla commands, portals) put their relative flags into the event, and as in Paper
the flags now survive a listener changing the destination (Arclight dropped them, and threw on vanilla's immutable
flag sets).

Player API, from the Paper patches named in `FusionCorePlayers`: `getClientOption`, `Player.Spigot#getPing`
(kept by Paper, dropped by Spigot), `isConnected` (also on `OfflinePlayer`), experience points (`applyMending`,
`giveExp(int, boolean)`, `calculateTotalExperiencePoints`, `setExperienceLevelAndProgress`,
`getExperiencePointsNeededForNextLevel`), attack cooldown, idle duration, warden warnings, win screen, elder guardian
effect, `sendOpLevel`, custom chat completions, `sendMultiBlockChange`, `sendEntityEffect`, string/BungeeCord action
bars, `sendMessage(ChatMessageType, …)`, resource packs with a hex hash, `boostElytra`, the deprecated ban helpers, and
on `HumanEntity` `getFishHook`, `isDeeplySleeping`, shoulder entity release, `setHurtDirection`, `openSign` and
`openAnvil/CartographyTable/Grindstone/Loom/SmithingTable/Stonecutter(Location, boolean force)` (a forced menu skips the
reach check, as in Paper); `LivingEntity#getHurtDirection`. Listing API (`listPlayer`/`unlistPlayer`/`isListed`):
Paper passes the viewer into each place that builds the tab-list info; Fusion Core instead rewrites the "listed" flag in
every `ClientboundPlayerInfoUpdatePacket` sent to the viewer (a copy, broadcasts share the packet), so plugins' and
mods' info packets respect it too.

Player API with core hooks; the state lives on the `CraftPlayer`, which outlives the `ServerPlayer` across
respawns on NeoForge: `setAffectsSpawning` (as Paper 1.21.1: mob despawning, spawners, zombie reinforcements, the
skeleton horse trap and its `SkeletonHorseTrapEvent` players; natural spawning is steered by
`PlayerNaturallySpawnCreaturesEvent`), `setFlyingFallDamage`, `getClientBrandName` (from the configuration-phase
`minecraft:brand` payload), `getLastLogin`/`getLastSeen` (saved as `Paper.LastLogin/LastSeen` in the player file and read
by `OfflinePlayer`), `OfflinePlayer#getPersistentDataContainer` (a read-only copy of the saved data for offline players),
`getResourcePackStatus`/`hasResourcePack`, sent chunks, `getHAProxyAddress` (always null: no PROXY protocol support).
Deviations: a player's view distance (`setViewDistance`/`setSendViewDistance`, 2–32 or -1 for the world's) can only
lower the world's distance, since vanilla loads chunks around players only as far as the world's distance, and view and
send distance are one value; a per-player simulation distance does not exist in vanilla, so `getSimulationDistance`
returns the world's and `setSimulationDistance` throws `UnsupportedOperationException`.

Adventure on players (Paper patch 0010): several `Audience` methods are empty by default in Adventure, so on
Fusion Core a plugin's call silently did nothing: boss bars (`showBossBar`/`hideBossBar`/`activeBossBars`), resource pack
requests (`sendResourcePacks` with callbacks, answered before `PlayerResourcePackStatusEvent` as in Paper;
`removeResourcePacks`, `clearResourcePacks`), `openBook`, `deleteMessage`, the tab `sendPlayerListHeader/Footer` (kept
apart, as in Paper) and `pointers()` (players, entities, command senders, worlds). `sendMessage(Component,
ChatType.Bound)` now goes out as a chat message decorated by the chat type, as in Paper. Deviations: each shown boss bar
is a vanilla `ServerBossEvent` updated through its setters by a bar listener (Paper makes the vanilla getters read the
bar; the packets are the same), bars follow a player's new `ServerPlayer` after a respawn and are taken off a player that
leaves; `BossBar#viewers()` stays empty, because Adventure looks its implementation up through a service loader that
cannot see the core; `sendMessage(SignedMessage, ChatType.Bound)` still sends the unsigned content (Adventure's default).

Item, menu and effect API (Paper server patches 0010, 0143, 0144, 0193, 0532, 0679, 0854, 0927, 0934, 1054; api
patch 0247): `ItemStack#asOne/asQuantity/add/subtract`, `withType`, `add/remove/get/hasItemFlag(s)`, `isRepairableBy`/`canRepair`,
`getI18NDisplayName`, `getMaxItemUseDuration` (with and without an entity), `enchantWithLevels(int, boolean, Random)`,
`ensureServerConversions`, `damage(int, LivingEntity)`, `computeTooltipLines` (with `TooltipContext`), the deprecated
`getRarity` (Paper's `io.papermc.paper.inventory.ItemRarity`); the same on `ItemFactory` (`ensureServerConversions`,
`getI18NDisplayName`, `enchantWithLevels`) and `UnsafeValues` (`isValidRepairItemStack`, `computeTooltipLines`,
`serializeItemAsJson`/`deserializeItemFromJson`, `createEmptyStack`). `MenuType#create(HumanEntity, Component)` (also
`MenuType.Typed`), with the title kept as a component. `Effect`: Paper's 27 constants for the 1.21.1 level events
(`WITHER_SPAWNED`, `TRIAL_SPAWNER_*`, `VAULT_*`, `SHOOT_WHITE_SMOKE`, …) are registered at startup with Paper's ids and data
types, reached through the `FusionCoreEnumConstants` rewrite and found by `Effect.getById`; `Effect#isApplicable`,
`STEP_SOUND` with `BlockData`, and `World/Player#playEffect` check data with it; `CraftEffect` computes the level-event data
for Paper's constants. Paper's plain `ItemStack` delegates to a hidden `CraftItemStack`; here the Craft-backed methods copy a
plain stack to a vanilla one, with the same result. Deviations: `Effect.STEP_SOUND.getData()` stays `Material` (Spigot's
single data class; Paper returns `BlockData`); JSON item data older than the server is updated with vanilla's DataFixer
(Paper uses its own converter); `enchantWithLevels(int, RegistryKeySet, Random)` and `MenuType` as `FeatureDependant` wait for
Paper's registry API. Found on the way: `PlainTextComponentSerializer.plainText()` does not translate translatable
components as on Paper (Adventure loads Paper's serializer providers through a service loader that cannot see the core).

Block and world API (Paper patches 0123, 0207, 0253, 0396, 0432, 0450, 0498, 0500, 0516, 0552, 0583, 0590, 0677,
0692, 0728, 0730, 0744, 0897, 0898, 0908, 0929, 0936, 1015, 1064): `Block` key (with the `Block.getBlockKey*` statics),
`isSolid/isBuildable/isBurnable/isReplaceable/isCollidable`, `isValidTool`, `getDestroySpeed` (also on `BlockData`, with
enchantments), `getComputedBiome`, `tick`/`fluidTick`, the `breakNaturally` overloads; `BlockState#getDrops`/`isCollidable`;
`BlockData#isRandomlyTicked`/`getCollisionShape`. `World`: blocks, locations and chunks by packed key, `isPositionLoaded`,
entity/chunk/block entity/player counts, coordinate scale, fixed time, infiniburn, `isDayTime`, `getEntity(UUID)`,
`sendGameEvent`, `findLightningRod/findLightningTarget`, void damage settings (enabled, amount, height offset; used by the
entity void check), `getRaid(id)`, `hasStructureAt`, ray traces from a `Position` with a block filter, the deprecated
`locateNearestBiome` overloads. `RegionAccessor`: computed biome, `FluidData` (Paper's fluid state API), world key, moon phase,
`hasCollisionsIn`, `lineOfSightExists`, spawning with a spawn reason. `WorldCreator`: key constructors and statics
(`ofKey`, `ofNameAndKey`; worlds are created under the creator's key, and a key that is taken is refused as in Paper),
`keepSpawnLoaded`. `Raid#getId/getBossBar`, `GameEvent#getRange/getVibrationLevel`, `Fluid.EMPTY`. Paper's enum
constants that Spigot's enums lack cannot be added as fields by a mixin; the core registers the value and plugin reads
of the field are rewritten to a getter in `FusionCoreEnumConstants`. Deviations: vanilla sends the chunks it loads, so
the world's send and no-tick view distances are its view distance (setting them sets it); void damage settings are
Paper's world config defaults (on, 4 damage, 64 below the bottom), changeable through the API but not saved; fluid data
of mod fluids is mapped like water and lava (source = falling data, flowing = flowing data); `getDestroySpeed` with
enchantments sums only mining efficiency modifiers (Paper sums every attribute modifier of the enchantments, the same
for vanilla); region accessors of world generation do not repeat Paper's region bounds check.

Entity, item, armor stand, beacon and living entity API (Paper patches 0036, 0141, 0280, 0655, 0866, 0413, 1060,
0593, 0132, 0428, 0639, 0118, 0217, 0219, 0429, 0064, 0566, 0199, 0204, 0147, 0316, 0557, 0793, 0240, 0252, 0367, 0393,
0366, 0768, 0967): `Entity#getOrigin`, `getEntitySpawnReason` (the first reason an entity is added with; `/summon` gives
COMMAND, mob and trial spawners SPAWNER/TRIAL_SPAWNER), `fromMobSpawner`, `lockFreezeTicks` (locked ticks neither grow in
powder snow nor thaw), `setPose(pose, fixed)`/`hasFixedPose`, `isTicking`, `broadcastHurtAnimation`, `spawnAt`; origin,
spawn reason (with Paper's fallback for entities saved without one), mob-spawner flag and freeze lock are saved under
Paper's NBT keys, so worlds move between Paper and Fusion Core with them. `Item#setCanMobPickup` (checked where a mob
looks for items, not saved, as in Paper), `setCanPlayerPickup`/`setWillAge` (Paper's magic pickup delay and age),
`get/setHealth`. `ArmorStand#setCanMove`, `setCanTick` (saved as `Paper.CanTickOverride`; a stand that does not tick
still syncs its pose and equipment), items by slot, disabled slots, rotations (Paper's `Rotations`). `Beacon#get/set/
resetEffectRange` (saved as `Paper.Range`, used by the beacon's effect sweep). `LivingEntity`: arrows and stingers,
shield blocking delay, `setKiller`, jump, body yaw, movement input, Paper's target block/entity ray traces
(`TargetBlockInfo`, `TargetEntityInfo`), `knockback`, `playPickupItemAnimation`, `swingHand`, `broadcastSlotBreak`,
`damageItemStack`, `canUseEquipmentSlot`, active item (`startUsingItem`, `completeUsingActiveItem`, remaining and used
time). Fix: Spigot's `Beacon#getEntitiesInRange` called CraftBukkit's `BeaconBlockEntity#getHumansInRange`, which
NeoForge's beacon does not have (NoSuchMethodError); the players are now collected as vanilla's effect sweep does, with
the custom range. Deviations: there is no Paper world config, so the armor stand tick default and the shield blocking
delay default are vanilla's (tick; 5 ticks), changeable per entity through the API; `damageItemStack` on a player in
creative mode, and on a stack that is not in a slot, damages through vanilla's player-less path, so
`PlayerItemDamageEvent` is not fired there (Paper fires it). `Entity#spawnAt` takes an entity that is not in a world yet
(`World#createEntity`; Paper's `UnsafeValues#deserializeEntity` is not ported yet).

Scoreboards, bans, witches, traders, thrown potions, brewing (Paper patches 0930, 0931, 0835, 0190-0192, 0256,
0523, 0557, 0554, 0674, 0900, 1061): `Objective`/`Score#numberFormat` (Paper's `NumberFormat`: blank, fixed, styled);
`Bukkit/Server#getBanList(BanListType)` and the `ProfileBanList` overloads Paper declares (Paper's profile, Bukkit's profile
with `Instant`/`Duration`); `WitchReadyPotionEvent`, `WitchThrowPotionEvent`, `WitchConsumePotionEvent` and
`Witch#get/setPotionUseTimeLeft`, `get/setDrinkingPotion`; `WanderingTrader#canDrinkPotion/canDrinkMilk` (not saved, as in
Paper) and `get/setWanderingTowards`; `ThrownPotion#getPotionMeta/setPotionMeta/splash` and `setItem` with any item;
Paper's splash fixes: a water bottle fires `WaterBottleSplashEvent` (a `PotionSplashEvent`) that decides who is hurt,
extinguished and rehydrated, a cancelled splash shows no particles, a lingering cloud whose effects a listener cleared
is not spawned unless `LingeringPotionSplashEvent#allowsEmptyCreation` is set; `BrewingStartEvent#get/setBrewingTime`,
`get/setRecipeBrewTime`, `BrewingStand` and `BrewingStandView#get/setRecipeBrewTime`. Deviations: Paper adds a third value
to `BrewingStandMenu`'s `ContainerData` for the recipe brew time; mods that build a brewing menu with vanilla's two
values would fail its size check, so the time lives on the brewing stand (on the menu for a plugin-made one) and only
the brew progress sent to the client is scaled to 400 ticks, as Paper's data slot does. `WaterBottleSplashEvent` reaches
the affected map through `PotionSplashEvent`'s accessors (Spigot keeps the field private). Splash events without a hit
(`splash()`) get a MISS hit result, so hit block and entity are null.

Potions, stews and block sounds (Paper patches 0669, 0656, 0356, 0526, 0976, 0824, 0289, API 0103): custom
potion mixes (`Bukkit/Server#getPotionBrewer`, `PotionBrewer#addPotionMix/removePotionMix/resetPotionMixes`,
`PotionMix` with predicate choices), checked before vanilla and mod recipes in NeoForge's `PotionBrewing` (slot checks,
brewing, result); `PotionEffectType#getEffectCategory/getEffectAttributes/getAttributeModifierAmount` (also on
`PotionEffectTypeWrapper`); `PotionEffect` `with*` copies and hidden effect (constructor, `getHiddenPotionEffect`,
`equals`/`hashCode`/serialization); entity effects keep the icon flag both ways and report the hidden effect (Spigot set
icon = particles); `LivingEntity#clearActivePotionEffects`; `SuspiciousEffectEntry` on `MushroomCow` (`getStewEffects`,
`setStewEffects`, entry overload, Paper's deprecated single-effect accessors that throw) and `SuspiciousStewMeta`;
`Block#getSoundGroup` (`BlockSoundGroup`). Deviations: NeoForge builds the brewing registry once per server and never
rebuilds it, so where Paper rebuilds it (data pack reload, `resetPotionMixes`) only the plugin mixes are dropped — before
`ServerResourcesReloadedEvent`, as in Paper; material and exact choices of a mix are tested directly instead of through
a vanilla `Ingredient` (same matching); sounds of mod blocks that are not in Bukkit's `Sound` enum come back as `null`
(as in Spigot's `getBlockSoundGroup`).

Components and data packs (Paper patches 0010, 0529): `PaperComponents` (serializers with the
server-language flattener, `resolveWithContext`; `UnsafeValues` now returns the same objects) and the Datapack API
(`Bukkit/Server#getDatapackManager`: list packs, refresh, enable or disable a pack, which reloads data packs and fires
`ServerResourcesReloadedEvent` with cause PLUGIN, as in Paper). Mod data packs are listed with the source NeoForge gives them.

Mob API (Paper patches 0244, 0353, 0205, 0535, 0585, 0811, 0253, 0557): `Mob#getPathfinder` (Paper's
`Pathfinder`), `Bukkit/Server#getMobGoals` (Paper's Mob Goal API with `VanillaGoal` keys), `lookAt`, head rotation
limits, `isLeftHanded`, `isAggressive`, `getPossibleExperienceReward`, `isInDaylight`, `setLootTable(table, seed)`;
`RangedEntity` on skeletons, drowned, illusioners, llamas, piglins, pillagers, snow golems, witches and withers;
`SchoolableFish` on cod, salmon and tropical fish (with Paper's guard against the vanilla NPE when a fish without a
leader stops following). Deviations: vanilla has no `UNKNOWN_BEHAVIOR` goal flag (Paper adds it to every goal without
flags), so the API reports goals without flags as `UNKNOWN_BEHAVIOR`, and a plugin goal of only that type runs without
excluding other such goals, as vanilla flagless goals do; goal keys of mod goals use the `minecraft` namespace like
Paper's, with characters a key does not allow replaced by `_`, and a mod goal whose mob class Paper's table does not know
gets the nearest mapped superclass (or `Mob`) instead of Paper's exception; `getPathfinder` returns a new, stateless
object each call.

Block experience: Spigot's `BlockBreakEvent#getExpToDrop` was always 0 in Arclight and `setExpToDrop` did
nothing, because NeoForge drops block experience itself in its `BlockDropsEvent`. The event now carries the default
Spigot gives it (survival, a tool that harvests the block; computed as NeoForge computes it, so mods' blocks and
enchantments count), and its value is handed to `BlockDropsEvent` at the highest priority (mods can still adjust it
after plugins). Experience a plugin sets for a block broken without drops (wrong tool) is dropped at the end of the
break, as in Spigot. Paper's `BlockDestroyEvent` experience works the same way (dropped with the items only). On
Forge/Fabric builds the default is 0.

Plugin reflection: on Forge/NeoForge, looking up a method or field of a class whose generic signatures name
a client-only class (`ServerPlayer` is one) threw, because Arclight resolved every generic signature and FML
refuses client classes on a dedicated server. The lookup now maps names by erased descriptors only.

The layer does not reach Paper's plugin bootstrap — `paper-plugin.yml`,
`PluginBootstrap`, `LifecycleEventOwner` — or Paper's Brigadier command API, so plugins built on
them (Backuper 4.x, CrazyCrates 4.x/5.x) still cannot load. That would be a much larger piece
of work.

## Pinned dependency: fabric-loader

`fabric-loader` and `fabric-api` are held at 0.18.2 / 0.116.7+1.21.1 rather than the
versions upstream carries. Do not bump them without re-testing a NeoForge server start.

Loom puts the mixin implementation named in fabric-loader's `fabric-installer.json` on
the compile classpath of every module, `fusioncore-common` included. fabric-loader 0.19.x
names sponge-mixin 0.17.x, which redeclared `@Redirect.at()` from `At` to `At[]`.
Everything compiled against it gets an array-valued `at`, while NeoForge's own mixin
(sponge-mixin 0.15.2) and the MixinExtras bundled with it read that member as a single
annotation:

```
java.lang.ClassCastException: class java.util.ArrayList cannot be cast to
  class org.objectweb.asm.tree.AnnotationNode
    at mixinextras.neoforge@0.5.3/...FactoryRedirectWrapperMixinTransformer.transform(:18)
    at ...MixinTransformerExtension.preApply
    at ...TargetClassContext.applyMixins
Fail to launch Arclight.
```

The server dies during mixin apply, before world load, and the exception goes to the
console rather than log4j, so `logs/latest.log` just stops mid-application.

This is not specific to Fusion Core. An unmodified build of the upstream base commit
(`d27101f1`) fails identically; upstream introduced it in `6eabd5d0`, where fabric-loader
went from 0.18.2 to 0.19.2. `c750d496` (fabric-loader 0.18.2, sponge-mixin 0.16.5, which
still declares `At at()`) is the last upstream state that starts. NeoForge stays at
21.1.250.

If a future upstream merge brings the bump back, the symptom is that same
ClassCastException; check with `javap -v` that a compiled `@Redirect` shows
`at=@...At(` and not `at=[@...At(`.

## Build notes

Building needs JDK 21 and a lot of patience on the first run: Spigot BuildTools clones and
compiles CraftBukkit before anything else can be compiled.

On a machine whose internet access goes through a proxy, set
`JAVA_TOOL_OPTIONS=-Djava.net.useSystemProxies=true` before building. Gradle forks child
JVMs — BuildTools among them — and those inherit `JAVA_TOOL_OPTIONS` but not
`HTTP_PROXY`/`HTTPS_PROXY`. Without it, BuildTools' clone of `hub.spigotmc.org` stalls
silently with no error. `build-fusioncore.ps1` sets this for you.

## Scope

The Arclight mod id, Java package names, mixin configuration and compatibility code remain
inherited from upstream. Fusion Core is a derivative project, not a clean-room implementation.
Existing copyright notices and the GPL-3.0 license are retained. Full Paper/Purpur API
compatibility is not claimed.

## Previous release

See the 0.1.2 source archive for the full change list of the 1.20.1/Forge line.
