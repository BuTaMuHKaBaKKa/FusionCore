package io.izzel.arclight.common.mixin.core.server.level;

import com.mojang.datafixers.DataFixer;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.bridge.core.server.level.ChunkMapBridge;
import io.izzel.arclight.common.mod.util.ArclightCallbackExecutor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.bukkit.craftbukkit.v.generator.CustomChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin implements ChunkMapBridge {

    // @formatter:off
    @Shadow @Nullable protected abstract ChunkHolder getUpdatingChunkIfPresent(long chunkPosIn);
    @Shadow protected abstract Iterable<ChunkHolder> getChunks();
    @Shadow protected abstract void tick();
    @Shadow @Final public ServerLevel level;
    @Shadow @Final @Mutable private RandomState randomState;
    @Shadow @Final @Mutable private ChunkGeneratorStructureState chunkGeneratorState;
    @Shadow @Final @Mutable private WorldGenContext worldGenContext;
    @Invoker("tick") public abstract void bridge$tick(BooleanSupplier hasMoreTime);
    @Invoker("setServerViewDistance") public abstract void bridge$setViewDistance(int i);
    // @formatter:on

    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$updateRandom(ServerLevel p_214836_, LevelStorageSource.LevelStorageAccess p_214837_, DataFixer p_214838_, StructureTemplateManager p_214839_, Executor p_214840_, BlockableEventLoop p_214841_, LightChunkGetter p_214842_, ChunkGenerator chunkGenerator, ChunkProgressListener p_214844_, ChunkStatusUpdateListener p_214845_, Supplier p_214846_, int p_214847_, boolean p_214848_, CallbackInfo ci) {
        this.bridge$setChunkGenerator(chunkGenerator);
    }

    @Redirect(method = "upgradeChunkTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;"))
    private ResourceKey<LevelStem> arclight$useTypeKey(ServerLevel serverWorld) {
        return ((WorldBridge) serverWorld).bridge$getTypeKey();
    }

    public final ArclightCallbackExecutor callbackExecutor = new ArclightCallbackExecutor();

    @Override
    public ArclightCallbackExecutor bridge$getCallbackExecutor() {
        return this.callbackExecutor;
    }

    @Override
    public ChunkHolder bridge$chunkHolderAt(long chunkPos) {
        return getUpdatingChunkIfPresent(chunkPos);
    }

    @Override
    public void bridge$setChunkGenerator(ChunkGenerator generator) {
        var rg = generator;
        if (rg instanceof CustomChunkGenerator custom) {
            rg = custom.getDelegate();
        }
        if (rg instanceof NoiseBasedChunkGenerator noise) {
            this.randomState = RandomState.create(noise.generatorSettings().value(), this.level.registryAccess().lookupOrThrow(Registries.NOISE), this.level.getSeed());
        } else {
            this.randomState = RandomState.create(NoiseGeneratorSettings.dummy(), this.level.registryAccess().lookupOrThrow(Registries.NOISE), this.level.getSeed());
        }
        this.chunkGeneratorState = generator.createState(level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET), this.randomState, level.getSeed());
        var old = this.worldGenContext;
        this.worldGenContext = new WorldGenContext(old.level(), generator, old.structureManager(), old.lightEngine(), old.mainThreadMailBox());
    }

    // Fusion Core: Paper's per-player view distance (Player#setViewDistance): it can lower the world's distance for one player
    @com.llamalad7.mixinextras.injector.ModifyReturnValue(method = "getPlayerViewDistance", at = @At("RETURN"))
    private int fusioncore$playerViewDistance(int distance, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.server.level.ServerPlayer player) {
        int own = io.izzel.arclight.common.mod.util.FusionCorePlayers.state(player).fusioncore$viewDistance();
        return own == -1 ? distance : Math.min(distance, own);
    }

    // Fusion Core: ChunkMap#acquireGeneration expects every chunk a generation task reaches to have a ChunkHolder. With mods that
    // keep chunks far away (Sable's plots) one can be missing, and the NPE stopped the server. The missing one is always a neighbour
    // (the chunk being generated has its holder), so it is scheduled one level past a full chunk, the most a neighbour of a full
    // chunk is ever asked for, and left to unload once generation lets go of it; each case is logged.

    // @formatter:off
    @Shadow @Final public it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap;
    @Shadow @Final public it.unimi.dsi.fastutil.longs.LongSet toDrop;
    @Shadow @Nullable abstract ChunkHolder updateChunkScheduling(long chunkPos, int newLevel, @Nullable ChunkHolder holder, int oldLevel);
    // @formatter:on

    @org.spongepowered.asm.mixin.Unique
    private static int fusioncore$missingHolders;

    @Inject(method = "acquireGeneration", cancellable = true, at = @At("HEAD"))
    private void fusioncore$missingHolder(long chunkPos, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.server.level.GenerationChunkHolder> cir) {
        if (this.updatingChunkMap.get(chunkPos) != null) {
            return;
        }
        int level = net.minecraft.server.level.ChunkLevel.byStatus(net.minecraft.server.level.FullChunkStatus.FULL) + 1;
        ChunkHolder holder = this.updateChunkScheduling(chunkPos, level, null, net.minecraft.server.level.ChunkLevel.MAX_LEVEL + 1);
        int count = ++fusioncore$missingHolders;
        if (count <= 20 || count % 1000 == 0) {
            io.izzel.arclight.common.mod.server.ArclightServer.LOGGER.warn("Chunk {} in {} was sent to generation without a ChunkHolder ({} time(s)); scheduled one instead of crashing",
                new net.minecraft.world.level.ChunkPos(chunkPos), this.level.dimension().location(), count);
        }
        if (holder == null) {
            return; // cannot happen at a loaded level; vanilla code runs and fails as before
        }
        ((io.izzel.arclight.common.bridge.core.world.server.ChunkHolderBridge) holder).bridge$updateHighestAllowedStatus((ChunkMap) (Object) this);
        this.toDrop.add(chunkPos);
        holder.increaseGenerationRefCount();
        cir.setReturnValue(holder);
    }
}
