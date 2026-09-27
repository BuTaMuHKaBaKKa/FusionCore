package io.izzel.arclight.common.mixin.core.server;

import com.mojang.datafixers.DataFixer;
import io.izzel.arclight.api.ArclightVersion;
import io.izzel.arclight.common.bridge.bukkit.CraftServerBridge;
import io.izzel.arclight.common.bridge.core.command.CommandSourceBridge;
import io.izzel.arclight.common.bridge.core.server.MinecraftServerBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.mod.ArclightConstants;
import io.izzel.arclight.common.mod.mixins.annotation.TransformAccess;
import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.common.mod.server.BukkitRegistry;
import io.izzel.arclight.common.mod.server.scheduler.PaperSchedulers;
import io.izzel.arclight.common.mod.server.world.border.ArclightBorderChangeListener;
import io.izzel.arclight.common.mod.server.world.border.ArclightDelegatedBorderListener;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.common.mod.util.BukkitOptionParser;
import io.izzel.arclight.common.util.IteratorUtil;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import it.unimi.dsi.fastutil.longs.LongIterator;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.Services;
import net.minecraft.server.TickTask;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeSource;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.v.CraftRegistry;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.scoreboard.CraftScoreboardManager;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.PluginLoadOrder;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spigotmc.WatchdogThread;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.management.ManagementFactory;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantBlockableEventLoop<TickTask> implements MinecraftServerBridge, CommandSourceBridge {

    // @formatter:off
    @Shadow private int tickCount;
    @Shadow protected long nextTickTimeNanos;
    @Shadow @Final static Logger LOGGER;
    @Shadow public abstract Commands getCommands();
    @Shadow protected abstract void updateMobSpawningFlags();
    @Shadow public abstract ServerLevel overworld();
    @Shadow private Map<ResourceKey<Level>, ServerLevel> levels;
    @Shadow protected abstract void setupDebugLevel(WorldData p_240778_1_);
    @Shadow protected WorldData worldData;
    @Shadow private static void setInitialSpawn(ServerLevel p_177897_, ServerLevelData p_177898_, boolean p_177899_, boolean p_177900_) { }
    @Shadow public abstract boolean isSpawningMonsters();
    @Shadow public abstract boolean isSpawningAnimals();
    @Shadow @Final public Executor executor;
    @Shadow public abstract RegistryAccess.Frozen registryAccess();
    @Shadow public MinecraftServer.ReloadableResources resources;
    @Shadow public abstract LayeredRegistryAccess<RegistryLayer> registries();
    @Shadow public abstract Iterable<ServerLevel> getAllLevels();
    // @formatter:on

    @Shadow private PlayerList playerList;

    public MinecraftServerMixin(String name) {
        super(name);
    }

    public WorldLoader.DataLoadContext worldLoader;
    private boolean forceTicks;
    public CraftServer server;
    public OptionSet options;
    public ConsoleCommandSender console;
    public RemoteConsoleCommandSender remoteConsole;
    public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();
    public int autosavePeriod;
    public Commands vanillaCommandDispatcher;
    private boolean hasStopped = false;
    private final Object stopLock = new Object();

    private static final int TPS = 20;
    private static final int TICK_TIME = 1000000000 / TPS;
    private static final int SAMPLE_INTERVAL = 100;
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static int currentTick = (int) (System.currentTimeMillis() / 50);
    public final double[] recentTps = {20, 20, 20}; // Fusion Core: 20 before the first sample, as Paper's averages start

    public boolean hasStopped() {
        synchronized (stopLock) {
            return hasStopped;
        }
    }

    @Override
    public boolean bridge$hasStopped() {
        return this.hasStopped();
    }

    @Override
    public double[] bridge$getRecentTps() { // Fusion Core
        return this.recentTps;
    }

    @Override
    public java.io.File bridge$getPluginsFolder() { // Fusion Core
        return (java.io.File) this.options.valueOf("plugins");
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void arclight$loadOptions(Thread p_236723_, LevelStorageSource.LevelStorageAccess p_236724_, PackRepository p_236725_, WorldStem worldStem, Proxy p_236727_, DataFixer p_236728_, Services p_236729_, ChunkProgressListenerFactory p_236730_, CallbackInfo ci) {
        String[] arguments = ManagementFactory.getRuntimeMXBean().getInputArguments().toArray(new String[0]);
        OptionParser parser = new BukkitOptionParser();
        try {
            options = parser.parse(arguments);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.vanillaCommandDispatcher = worldStem.dataPackResources().getCommands();
        this.worldLoader = ArclightCaptures.getDataLoadContext();
        ArclightServer.setMinecraftServer((MinecraftServer) (Object) this);
    }

    @Decorate(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;"))
    private ServerStatus arclight$initTickParam(MinecraftServer instance, @Local(allocate = "tickSection") long tickSection, @Local(allocate = "tickCount") long tickCount) throws Throwable {
        var serverStatus = (ServerStatus) DecorationOps.callsite().invoke(instance);
        Arrays.fill(recentTps, 20);
        tickSection = Util.getMillis();
        tickCount = 1;
        DecorationOps.blackhole().invoke(tickSection, tickCount);
        return serverStatus;
    }

    @Decorate(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;startMetricsRecordingTick()V"))
    private void arclight$updateTickParam(MinecraftServer instance, @Local(allocate = "tickSection") long tickSection, @Local(allocate = "tickCount") long tickCount) throws Throwable {
        if (tickCount++ % SAMPLE_INTERVAL == 0) {
            long curTime = Util.getMillis();
            double currentTps = 1E3 / (curTime - tickSection) * SAMPLE_INTERVAL;
            recentTps[0] = calcTps(recentTps[0], 0.92, currentTps); // 1/exp(5sec/1min)
            recentTps[1] = calcTps(recentTps[1], 0.9835, currentTps); // 1/exp(5sec/5min)
            recentTps[2] = calcTps(recentTps[2], 0.9945, currentTps); // 1/exp(5sec/15min)
            tickSection = curTime;
        }
        DecorationOps.blackhole().invoke(tickSection, tickCount);
        currentTick = (int) (System.currentTimeMillis() / 50);
        DecorationOps.callsite().invoke(instance);
    }

    @Decorate(method = "runServer", at = @At(value = "INVOKE", remap = false, target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void arclight$warnOnLoad(Logger instance, String s, Object o, Object o2) throws Throwable {
        if (server.getWarnOnOverload()) {
            DecorationOps.callsite().invoke(instance, s, o, o2);
        }
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;onServerExit()V"))
    private void arclight$watchdogExit(CallbackInfo ci) {
        WatchdogThread.doStop();
    }

    private static double calcTps(double avg, double exp, double tps) {
        return (avg * exp) + (tps * (1 - exp));
    }

    @Inject(method = "stopServer", cancellable = true, at = @At("HEAD"))
    public void arclight$setStopped(CallbackInfo ci) {
        synchronized (stopLock) {
            if (hasStopped) {
                ci.cancel();
                return;
            }
            hasStopped = true;
        }
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", remap = false, ordinal = 0, shift = At.Shift.AFTER, target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V"))
    public void arclight$unloadPlugins(CallbackInfo ci) {
        if (this.server != null) {
            this.server.disablePlugins();
        }
    }

    @Decorate(method = "createLevels", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"))
    private Iterator<Map.Entry<ResourceKey<LevelStem>, LevelStem>> arclight$skipBukkitLevels(Set<Map.Entry<ResourceKey<LevelStem>, LevelStem>> instance) throws Throwable {
        final var iterator = (Iterator<Map.Entry<ResourceKey<LevelStem>, LevelStem>>) DecorationOps.callsite().invoke(instance);
        if (ArclightConfig.spec().getExperimental().canOverrideWorldgen()) {
            return IteratorUtil.filter(iterator, it -> {
                final var location = it.getKey().location();
                if (location.getNamespace().equals("bukkit")) {
                    ArclightServer.LOGGER.info("Deferred {} custom dimension creation", location);
                    return false;
                } else {
                    return true;
                }
            });
        } else {
            return iterator;
        }
    }

    @Inject(method = "createLevels", at = @At("RETURN"))
    public void arclight$enablePlugins(ChunkProgressListener p_240787_1_, CallbackInfo ci) {
        this.bridge$forge$unlockRegistries();
        this.server.enablePlugins(PluginLoadOrder.POSTWORLD);
        this.bridge$forge$lockRegistries();
        this.server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
    }

    private void executeModerately() {
        this.runAllTasks();
        this.bridge$drainQueuedTasks();
        java.util.concurrent.locks.LockSupport.parkNanos("executing tasks", 1000L);
    }

    @Override
    public void bridge$drainQueuedTasks() {
        while (!processQueue.isEmpty()) {
            processQueue.remove().run();
        }
    }

    @Inject(method = "haveTime", cancellable = true, at = @At("HEAD"))
    private void arclight$forceAheadOfTime(CallbackInfoReturnable<Boolean> cir) {
        if (this.forceTicks) cir.setReturnValue(true);
    }

    @Inject(method = "createLevels", at = @At(value = "NEW", ordinal = 0, target = "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/world/level/storage/ServerLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;Lnet/minecraft/server/level/progress/ChunkProgressListener;ZJLjava/util/List;ZLnet/minecraft/world/RandomSequences;)Lnet/minecraft/server/level/ServerLevel;"))
    private void arclight$registerEnv(ChunkProgressListener p_240787_1_, CallbackInfo ci) {
        BukkitRegistry.registerEnvironments(this.registryAccess().registryOrThrow(Registries.LEVEL_STEM));
    }

    @Decorate(method = "createLevels", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/border/WorldBorder;)Lnet/minecraft/world/level/border/BorderChangeListener$DelegateBorderChangeListener;"))
    private BorderChangeListener.DelegateBorderChangeListener arclight$configurableDelegatedListener(WorldBorder arg) throws Throwable {
        // Arclight: move world border listener initialization to world registration
        return new ArclightDelegatedBorderListener(arg, (BorderChangeListener.DelegateBorderChangeListener) DecorationOps.callsite().invoke(arg));
    }

    @Decorate(method = "createLevels", at = @At(value = "INVOKE", remap = false, target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object arclight$worldInit(Map<Object, Object> instance, Object k, Object v, ChunkProgressListener chunkProgressListener) throws Throwable {
        if (v instanceof ServerLevel level) {
            if (((CraftServer) Bukkit.getServer()).scoreboardManager == null) {
                ((CraftServer) Bukkit.getServer()).scoreboardManager = new CraftScoreboardManager((MinecraftServer) (Object) this, level.getScoreboard());
            }
            if (((WorldBridge) level).bridge$getGenerator() != null) {
                level.bridge$getWorld().getPopulators().addAll(
                    ((WorldBridge) level).bridge$getGenerator().getDefaultPopulators(
                        level.bridge$getWorld()));
            }
            Bukkit.getPluginManager().callEvent(new WorldInitEvent(level.bridge$getWorld()));

            // Arclight: move world border listener initialization to world registration
            // Arclight: ArclightBorderChangeListener is singleton so won't be added more than once
            // Arclight: since it seems that we can't apply multiple Decorators to a target on Forge...
            level.getWorldBorder().addListener(ArclightBorderChangeListener.typed());
        }
        return DecorationOps.callsite().invoke(instance, k, v);
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public final void prepareLevels(ChunkProgressListener listener) {
        ServerLevel serverworld = this.overworld();
        this.forceTicks = true;
        LOGGER.info("Preparing start region for dimension {}", serverworld.dimension().location());
        BlockPos blockpos = serverworld.getSharedSpawnPos();
        listener.updateSpawnPos(new ChunkPos(blockpos));
        ServerChunkCache serverchunkprovider = serverworld.getChunkSource();
        this.nextTickTimeNanos = Util.getNanos();
        serverworld.setDefaultSpawnPos(blockpos, serverworld.getSharedSpawnAngle());
        int i = serverworld.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS); // CraftBukkit - per-world
        int j = i > 0 ? Mth.square(ChunkProgressListener.calculateDiameter(i)) : 0;

        while (serverchunkprovider.getTickingGenerated() < j) {
            // CraftBukkit start
            // this.nextTickTimeNanos = SystemUtils.getNanos() + MinecraftServer.PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
            this.executeModerately();
        }

        this.executeModerately();

        for (ServerLevel serverWorld : this.levels.values()) {
            if (serverWorld.bridge$getWorld().getKeepSpawnInMemory()) {
                ForcedChunksSavedData forcedchunkssavedata = serverWorld.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
                if (forcedchunkssavedata != null) {
                    LongIterator longiterator = forcedchunkssavedata.getChunks().iterator();

                    while (longiterator.hasNext()) {
                        long k = longiterator.nextLong();
                        ChunkPos chunkpos = new ChunkPos(k);
                        serverWorld.getChunkSource().updateChunkForced(chunkpos, true);
                    }
                    this.bridge$forge$reinstatePersistentChunks(serverWorld, forcedchunkssavedata);
                }
            }
            Bukkit.getPluginManager().callEvent(new WorldLoadEvent(serverWorld.bridge$getWorld()));
        }

        this.executeModerately();
        listener.stop();
        this.updateMobSpawningFlags();
        this.forceTicks = false;
    }

    // bukkit methods
    public void initWorld(ServerLevel serverWorld, ServerLevelData worldInfo, WorldData saveData, WorldOptions worldOptions) {
        boolean flag = saveData.isDebugWorld();
        if (((WorldBridge) serverWorld).bridge$getGenerator() != null) {
            serverWorld.bridge$getWorld().getPopulators().addAll(
                ((WorldBridge) serverWorld).bridge$getGenerator().getDefaultPopulators(
                    serverWorld.bridge$getWorld()));
        }
        WorldBorder worldborder = serverWorld.getWorldBorder();
        worldborder.applySettings(worldInfo.getWorldBorder());

        // Arclight: move world border listener initialization to world registration
        playerList.addWorldborderListener(serverWorld);

        // Call WorldInitEvent for Bukkit created world
        // Before any chunk is loaded/generated.
        // This makes delayed configurate possible.
        // Calling multiple times is OK since Spigot also do so.
        // See [PlotSquared] BukkitSetupUtils#setupWorld(PlotAreaBuilder).
        // See CraftServer.
        // CraftBukkit - SPIGOT-5569: Call WorldInitEvent before any chunks are generated
        this.server.getPluginManager().callEvent(new WorldInitEvent(serverWorld.bridge$getWorld()));

        if (!worldInfo.isInitialized()) {
            try {
                setInitialSpawn(serverWorld, worldInfo, worldOptions.generateBonusChest(), flag);
                worldInfo.setInitialized(true);
                if (flag) {
                    this.setupDebugLevel(this.worldData);
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception initializing level");
                try {
                    serverWorld.fillReportDetails(crashreport);
                } catch (Throwable throwable2) {
                    // empty catch block
                }
                throw new ReportedException(crashreport);
            }
            worldInfo.setInitialized(true);
        }
    }

    // bukkit methods
    public void prepareLevels(ChunkProgressListener listener, ServerLevel serverWorld) {
        this.bridge$forge$markLevelsDirty();
        if (!serverWorld.bridge$getWorld().getKeepSpawnInMemory()) {
            return;
        }
        this.forceTicks = true;
        LOGGER.info("Preparing start region for dimension {}", serverWorld.dimension().location());
        BlockPos blockpos = serverWorld.getSharedSpawnPos();
        listener.updateSpawnPos(new ChunkPos(blockpos));
        ServerChunkCache serverchunkprovider = serverWorld.getChunkSource();
        this.nextTickTimeNanos = Util.getNanos();
        serverWorld.setDefaultSpawnPos(blockpos, serverWorld.getSharedSpawnAngle());
        int i = serverWorld.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS); // CraftBukkit - per-world
        int j = i > 0 ? Mth.square(ChunkProgressListener.calculateDiameter(i)) : 0;

        while (serverchunkprovider.getTickingGenerated() < j) {
            // CraftBukkit start
            // this.nextTickTimeNanos = SystemUtils.getNanos() + MinecraftServer.PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
            this.executeModerately();
        }

        this.executeModerately();

        ForcedChunksSavedData forcedchunkssavedata = serverWorld.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
        if (forcedchunkssavedata != null) {
            LongIterator longiterator = forcedchunkssavedata.getChunks().iterator();

            while (longiterator.hasNext()) {
                long k = longiterator.nextLong();
                ChunkPos chunkpos = new ChunkPos(k);
                serverWorld.getChunkSource().updateChunkForced(chunkpos, true);
            }
            this.bridge$forge$reinstatePersistentChunks(serverWorld, forcedchunkssavedata);
        }
        this.executeModerately();
        listener.stop();
        // this.updateMobSpawningFlags();
        serverWorld.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
        this.forceTicks = false;
    }

    // bukkit callbacks
    public void addLevel(ServerLevel level) {
        this.levels.put(level.dimension(), level);
        this.arclight$onServerLoad(level);
        this.bridge$forge$markLevelsDirty();
    }

    public void removeLevel(ServerLevel level) {
        this.levels.remove(level.dimension());
        this.arclight$onServerUnload(level);
        this.bridge$forge$markLevelsDirty();
        ((CraftServerBridge) Bukkit.getServer()).bridge$removeWorld(level);
    }

    @Inject(method = "tickChildren", at = @At("HEAD"))
    public void arclight$runScheduler(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        ArclightConstants.currentTick = (int) (System.currentTimeMillis() / 50);
        this.server.getScheduler().mainThreadHeartbeat(this.tickCount);
        PaperSchedulers.tick(); // Fusion Core: Paper's Folia schedulers (patch 0843)
        io.izzel.arclight.common.mod.util.FusionCoreAdventureProviders.CALLBACK_MANAGER.handleQueue(this.tickCount); // Fusion Core: Paper's click callbacks
        this.bridge$drainQueuedTasks();
    }

    // Fusion Core: Paper's Server#isTickingWorlds (patch 0707), true from the first world tick to the end of the world loop
    @Inject(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tick(Ljava/util/function/BooleanSupplier;)V"))
    private void fusioncore$tickingWorlds(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCorePaperServer.setTickingWorlds(true);
    }

    @Inject(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getConnection()Lnet/minecraft/server/network/ServerConnectionListener;"))
    private void fusioncore$worldsTicked(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCorePaperServer.setTickingWorlds(false);
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/MinecraftServer;saveAllChunks(ZZZ)Z"))
    private void arclight$unloadLevel(CallbackInfo ci) {
        for (var serverLevel : this.getAllLevels()) {
            ((CraftServerBridge) Bukkit.getServer()).bridge$removeWorld(serverLevel);
        }
    }

    @Inject(method = "saveAllChunks", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private void arclight$skipSave(boolean suppressLog, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!this.levels.isEmpty());
    }

    @Inject(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/WorldData;setDataConfiguration(Lnet/minecraft/world/level/WorldDataConfiguration;)V"))
    private void arclight$syncCommand(CallbackInfo ci) {
        // Fusion Core: Paper fires LifecycleEvents.COMMANDS again after a data pack reload and rebuilds the help map (patch 0965)
        io.papermc.paper.command.brigadier.PaperCommands.INSTANCE.fireReload();
        io.izzel.arclight.common.mod.util.FusionCorePaperServer.reloadHelpMap();
        this.server.syncCommands();
    }

    @Inject(method = "getServerModName", remap = false, cancellable = true, at = @At("RETURN"))
    private void arclight$brand(CallbackInfoReturnable<String> cir) {
        // Fusion Core: branding prefix
        cir.setReturnValue("Fusion Core " + cir.getReturnValue());
    }

    private boolean arclight$skipWatchdogSetTime = false;

    @Override
    public void arclight$extendNextTickTimeTo(TimeSource.NanoTimeSource timeSource) {
        if (!arclight$skipWatchdogSetTime) {
            this.nextTickTimeNanos = timeSource.getAsLong();
        }
    }

    protected void arclight$tickSpigotWatchdogInternal() {
        try {
            arclight$skipWatchdogSetTime = true;
            WatchdogThread.tick();
        } finally {
            arclight$skipWatchdogSetTime = false;
        }
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void arclight$tickWatchdog(CallbackInfo ci) {
        arclight$tickSpigotWatchdogInternal();
    }

    @Override
    public void bridge$setConsole(ConsoleCommandSender console) {
        this.console = console;
    }

    @Override
    public void bridge$setServer(CraftServer server) {
        this.server = server;
    }

    // Used for one-shot cache access
    @Override
    public CraftServer bridge$getServer() {
        if (this.server == null) {
            throw new IllegalStateException("CraftServer has not been initialized yet");
        }
        return this.server;
    }

    @Override
    public RemoteConsoleCommandSender bridge$getRemoteConsole() {
        return remoteConsole;
    }

    @Override
    public void bridge$queuedProcess(Runnable runnable) {
        processQueue.add(runnable);
    }

    public CommandSender getBukkitSender(CommandSourceStack wrapper) {
        return console;
    }

    @Override
    public CommandSender bridge$getBukkitSender(CommandSourceStack wrapper) {
        return getBukkitSender(wrapper);
    }

    @Override
    public Commands bridge$getVanillaCommands() {
        return this.vanillaCommandDispatcher;
    }

    public boolean isDebugging() {
        return false;
    }

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static MinecraftServer getServer() {
        return Bukkit.getServer() instanceof CraftServer ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    @Deprecated
    private static RegistryAccess getDefaultRegistryAccess() {
        return CraftRegistry.getMinecraftRegistry();
    }

    /** Fusion Core: Paper's ServerResourcesReloadedEvent after a data pack reload; plugin potion mixes are cleared first. */
    @Inject(method = "reloadResources", at = @At("RETURN"))
    private void fusioncore$resourcesReloaded(java.util.Collection<String> packs, CallbackInfoReturnable<java.util.concurrent.CompletableFuture<Void>> cir) {
        io.papermc.paper.event.server.ServerResourcesReloadedEvent.Cause cause = io.izzel.arclight.common.mod.util.FusionCoreEvents.takeReloadCause();
        cir.getReturnValue().thenRun(() -> {
            io.izzel.arclight.common.mod.util.FusionCorePotions.resetPotionMixes();
            org.bukkit.Bukkit.getPluginManager().callEvent(new io.papermc.paper.event.server.ServerResourcesReloadedEvent(cause));
        });
    }

    // Fusion Core: Paper's ServerTickStartEvent / ServerTickEndEvent (Paper patch 0283). The end event carries the tick's
    // duration in ms and the nanoseconds left until the next tick is due (vanilla's nextTickTimeNanos, so /tick rate
    // and sprinting are taken into account, where Paper computes it from its own 50 ms loop)

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void fusioncore$tickStartEvent(java.util.function.BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (com.destroystokyo.paper.event.server.ServerTickStartEvent.getHandlerList().getRegisteredListeners().length > 0) {
            Bukkit.getPluginManager().callEvent(new com.destroystokyo.paper.event.server.ServerTickStartEvent(this.tickCount + 1));
        }
    }

    @Inject(method = "tickServer", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", args = "ldc=tallying"))
    private void fusioncore$tickEndEvent(java.util.function.BooleanSupplier hasTimeLeft, CallbackInfo ci, @com.llamalad7.mixinextras.sugar.Local(ordinal = 0) long tickStart) {
        if (com.destroystokyo.paper.event.server.ServerTickEndEvent.getHandlerList().getRegisteredListeners().length > 0) {
            long end = Util.getNanos();
            Bukkit.getPluginManager().callEvent(new com.destroystokyo.paper.event.server.ServerTickEndEvent(this.tickCount, (end - tickStart) / 1_000_000D, this.nextTickTimeNanos - end));
        }

    }

    // Fusion Core: Paper's tick time rings (patch "Add tick times API and /mspt command"): public fields under Paper's names, read
    // by plugins through reflection (TabTPS); filled at the end of each tick from vanilla's own measurement
    @io.izzel.arclight.common.mod.mixins.annotation.TransformAccess(org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_FINAL)
    private final io.izzel.arclight.common.mod.util.FusionCoreTickTimes tickTimes5s = new io.izzel.arclight.common.mod.util.FusionCoreTickTimes(100);
    @io.izzel.arclight.common.mod.mixins.annotation.TransformAccess(org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_FINAL)
    private final io.izzel.arclight.common.mod.util.FusionCoreTickTimes tickTimes10s = new io.izzel.arclight.common.mod.util.FusionCoreTickTimes(200);
    @io.izzel.arclight.common.mod.mixins.annotation.TransformAccess(org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_FINAL)
    private final io.izzel.arclight.common.mod.util.FusionCoreTickTimes tickTimes60s = new io.izzel.arclight.common.mod.util.FusionCoreTickTimes(1200);

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void fusioncore$tickTimes(java.util.function.BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCoreTickTimes.record(this.tickTimes5s, this.tickTimes10s, this.tickTimes60s,
            this.tickCount, ((MinecraftServer) (Object) this).getTickTimesNanos()[this.tickCount % 100]);
    }
}
