package io.izzel.arclight.common.mixin.core.world.level;

import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.bridge.core.world.level.border.WorldBorderBridge;
import io.izzel.arclight.common.mod.ArclightConstants;
import io.izzel.arclight.common.mod.mixins.annotation.CreateConstructor;
import io.izzel.arclight.common.mod.mixins.annotation.ShadowConstructor;
import io.izzel.arclight.common.mod.mixins.annotation.TransformAccess;
import io.izzel.arclight.common.mod.server.world.ArclightWorldConfig;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.common.mod.util.DistValidate;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.BitRandomSource;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.block.CapturedBlockState;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.craftbukkit.v.util.CraftSpawnCategory;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.ChunkGenerator;
import org.objectweb.asm.Opcodes;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(Level.class)
public abstract class LevelMixin implements WorldBridge, LevelAccessor, LevelWriter {

    // @formatter:off
    @Shadow @Nullable public BlockEntity getBlockEntity(BlockPos pos) { return null; }
    @Shadow public abstract BlockState getBlockState(BlockPos pos);
    @Shadow public abstract WorldBorder getWorldBorder();
    @Shadow @Final private WorldBorder worldBorder;
    @Shadow public abstract long getDayTime();
    @Shadow public abstract MinecraftServer getServer();
    @Shadow public abstract LevelData getLevelData();
    @Shadow public abstract ResourceKey<Level> dimension();
    @Shadow public abstract DimensionType dimensionType();
    @Shadow public abstract void setBlocksDirty(BlockPos arg, BlockState arg2, BlockState arg3);
    @Shadow @Final public boolean isClientSide;
    @Shadow public abstract void sendBlockUpdated(BlockPos arg, BlockState arg2, BlockState arg3, int i);
    @Shadow public abstract void updateNeighbourForOutputSignal(BlockPos arg, Block arg2);
    @Shadow public abstract void onBlockStateChange(BlockPos arg, BlockState arg2, BlockState arg3);
    @Shadow public abstract RegistryAccess registryAccess();
    @Accessor("thread") public abstract Thread arclight$getMainThread();
    // @formatter:on

    protected CraftWorld world;
    public boolean pvpMode;
    public final Object2LongOpenHashMap<SpawnCategory> ticksPerSpawnCategory = new Object2LongOpenHashMap<>();
    public boolean populating;
    public org.bukkit.generator.ChunkGenerator generator;
    protected org.bukkit.World.Environment environment;
    protected org.bukkit.generator.BiomeProvider biomeProvider;
    public org.spigotmc.SpigotWorldConfig spigotConfig;
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static BlockPos lastPhysicsProblem; // Spigot
    public boolean preventPoiUpdated = false;
    public Map<BlockPos, CapturedBlockState> capturedBlockStates = new java.util.LinkedHashMap<>();
    public Map<BlockPos, BlockEntity> capturedTileEntities = new HashMap<>();

    @Unique private boolean arclight$isActual;

    @ShadowConstructor
    public void arclight$constructor(WritableLevelData worldInfo, ResourceKey<Level> dimension, RegistryAccess registryAccess, final Holder<DimensionType> dimensionType, Supplier<ProfilerFiller> profiler, boolean isRemote, boolean isDebug, long seed, int maxNeighborUpdate) {
        throw new RuntimeException();
    }

    @CreateConstructor
    public void arclight$constructor(WritableLevelData worldInfo, ResourceKey<Level> dimension, RegistryAccess registryAccess, final Holder<DimensionType> dimensionType, Supplier<ProfilerFiller> profiler, boolean isRemote, boolean isDebug, long seed, int maxNeighborUpdate, org.bukkit.generator.ChunkGenerator gen, org.bukkit.generator.BiomeProvider biomeProvider, org.bukkit.World.Environment env) {
        arclight$constructor(worldInfo, dimension, registryAccess, dimensionType, profiler, isRemote, isDebug, seed, maxNeighborUpdate);
        this.generator = gen;
        this.environment = env;
        this.biomeProvider = biomeProvider;
    }

    @SuppressWarnings({"DefaultAnnotationParam", "UnnecessaryUnsafe"})
    // InitAuther97: unsafe = true can't be removed because unsafe is default to false on Forge
    @Inject(method = "<init>", at = @At(value = "CTOR_HEAD", unsafe = true))
    private void arclight$preInit(WritableLevelData writableLevelData, ResourceKey resourceKey, RegistryAccess registryAccess, Holder holder, Supplier supplier, boolean bl, boolean bl2, long l, int i, CallbackInfo ci) {
        this.arclight$isActual = DistValidate.isValid((LevelAccessor) (Object) this);
    }

    // InitAuther97: inject later than ironsspellbooks, see their LevelMixin
    @Inject(method = "<init>", at = @At("RETURN"), order = 1001)
    private void arclight$init(WritableLevelData info, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimType, Supplier<ProfilerFiller> profiler, boolean isRemote, boolean isDebug, long seed, int maxNeighborUpdates, CallbackInfo ci) {
        ((WorldBorderBridge) this.worldBorder).bridge$setWorld((Level) (Object) this);
        for (SpawnCategory spawnCategory : SpawnCategory.values()) {
            if (CraftSpawnCategory.isValidForLimits(spawnCategory)) {
                this.ticksPerSpawnCategory.put(spawnCategory, this.getCraftServer().getTicksPerSpawns(spawnCategory));
            }
        }
    }

    @Override
    public Map<BlockPos, CapturedBlockState> bridge$getCapturedBlockState() {
        return this.capturedBlockStates;
    }

    @Override
    public boolean arclight$isActual() {
        return arclight$isActual;
    }

    @Override
    public Map<BlockPos, BlockEntity> bridge$getCapturedBlockEntity() {
        return this.capturedTileEntities;
    }

    @Override
    public void bridge$setLastPhysicsProblem(BlockPos pos) {
        lastPhysicsProblem = pos;
    }

    @Override
    public Object2LongOpenHashMap<SpawnCategory> bridge$ticksPerSpawnCategory() {
        return this.ticksPerSpawnCategory;
    }

    public abstract ResourceKey<LevelStem> getTypeKey();

    @Override
    public ResourceKey<LevelStem> bridge$getTypeKey() {
        return getTypeKey();
    }

    @Override
    public SpigotWorldConfig bridge$spigotConfig() {
        if (spigotConfig == null) {
            return ArclightWorldConfig.DEFAULT;
        }
        return this.spigotConfig;
    }

    // Fusion Core: Paper's Anti-Xray, a block that stops hiding its neighbours reveals them (Paper patch 0992)
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState fusioncore$antiXrayChange(LevelChunk chunk, BlockPos pos, BlockState state, boolean moved, com.llamalad7.mixinextras.injector.wrapoperation.Operation<BlockState> original,
                                                 @com.llamalad7.mixinextras.sugar.Local(argsOnly = true, ordinal = 0) int flags, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true, ordinal = 1) int depth) {
        BlockState old = original.call(chunk, pos, state, moved);
        if (old != null && (Object) this instanceof net.minecraft.server.level.ServerLevel) {
            io.izzel.arclight.common.mod.util.FusionCoreAntiXray.controller((Level) (Object) this).onBlockChange((Level) (Object) this, pos, state, old, flags, depth);
        }
        return old;
    }

    // Fusion Core: Spigot resets the max-tnt-per-tick counter at the end of tickBlockEntities
    @Inject(method = "tickBlockEntities", at = @At("RETURN"))
    private void fusioncore$resetPrimedTnt(CallbackInfo ci) {
        if (this.spigotConfig != null) {
            this.spigotConfig.currentPrimedTnt = 0;
        }
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
        at = @At("HEAD"), cancellable = true)
    private void arclight$hooks(BlockPos pos, BlockState newState, int flags, int i, CallbackInfoReturnable<Boolean> cir) {
        if (!ArclightCaptures.setLastEntityChangeBlockResult(processCaptures(pos, newState, flags))) {
            cir.setReturnValue(false);
        }
    }

    private boolean processCaptures(BlockPos pos, BlockState newState, int flags) {
        Entity entityChangeBlock = ArclightCaptures.getEntityChangeBlock();
        if (entityChangeBlock != null) {
            return CraftEventFactory.callEntityChangeBlockEvent(entityChangeBlock, pos, newState);
        }
        return true;
    }

    public void notifyAndUpdatePhysics(BlockPos pos, LevelChunk chunk, BlockState oldBlock, BlockState newBlock, BlockState actualBlock, int i, int j) {
        this.bridge$forge$notifyAndUpdatePhysics(pos, chunk, oldBlock, newBlock, i, j);
    }

    @Override
    public void bridge$forge$notifyAndUpdatePhysics(BlockPos pos, @Nullable LevelChunk levelchunk, BlockState oldBlock, BlockState newBlock, int j, int k) {
        Block block = newBlock.getBlock();
        BlockState blockstate1 = this.getBlockState(pos);
        if (blockstate1 == newBlock) {
            if (oldBlock != blockstate1) {
                this.setBlocksDirty(pos, oldBlock, blockstate1);
            }

            if ((j & 2) != 0 && (!this.isClientSide || (j & 4) == 0) && (this.isClientSide || levelchunk == null || levelchunk.getFullStatus() != null && levelchunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING))) {
                this.sendBlockUpdated(pos, oldBlock, newBlock, j);
            }

            if ((j & 1) != 0) {
                this.blockUpdated(pos, oldBlock.getBlock());
                if (!this.isClientSide && newBlock.hasAnalogOutputSignal()) {
                    this.updateNeighbourForOutputSignal(pos, block);
                }
            }

            if ((j & 16) == 0 && k > 0) {
                int i = j & -34;
                oldBlock.updateIndirectNeighbourShapes(this, pos, i, k - 1);
                try {
                    // Fusion Core: no listener, no Bukkit block, data and event for every shape update
                    if (this.world != null && BlockPhysicsEvent.getHandlerList().getRegisteredListeners().length > 0) {
                        BlockPhysicsEvent event = new BlockPhysicsEvent(CraftBlock.at(this, pos), CraftBlockData.fromData(newBlock));
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            return;
                        }
                    }
                } catch (StackOverflowError e) {
                    lastPhysicsProblem = pos;
                }
                newBlock.updateNeighbourShapes(this, pos, i, k - 1);
                newBlock.updateIndirectNeighbourShapes(this, pos, i, k - 1);
            }

            if (!this.preventPoiUpdated) {
                this.onBlockStateChange(pos, oldBlock, blockstate1);
            }
        }
    }

    public CraftServer getCraftServer() {
        return (CraftServer) Bukkit.getServer();
    }

    public CraftWorld getWorld() {
        if (this.world == null) {
            throw new UnsupportedOperationException(String.format("World %s does not have a CraftWorld. This method should not be invoked on this world for any reason.", dimension().location()));
        }
        return this.world;
    }

    public BlockEntity getBlockEntity(BlockPos pos, boolean validate) {
        return getBlockEntity(pos);
    }

    @Override
    public BlockEntity bridge$getTileEntity(BlockPos pos, boolean validate) {
        return getBlockEntity(pos, validate);
    }

    @Override
    public CraftServer bridge$getServer() {
        return (CraftServer) Bukkit.getServer();
    }

    @Override
    public CraftWorld bridge$getWorld() {
        return this.getWorld();
    }

    @Override
    public boolean bridge$isPvpMode() {
        return this.pvpMode;
    }

    @Override
    public boolean bridge$isPopulating() {
        return this.populating;
    }

    @Override
    public void bridge$setPopulating(boolean populating) {
        this.populating = populating;
    }

    @Override
    public ChunkGenerator bridge$getGenerator() {
        return generator;
    }

    @Override
    public boolean bridge$addEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        if (getWorld().getHandle() != (Object) this) {
            return ((WorldBridge) getWorld().getHandle()).bridge$addEntity(entity, reason);
        } else {
            this.bridge$pushAddEntityReason(reason);
            return this.addFreshEntity(entity);
        }
    }

    @Override
    public void bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason reason) {
        if (getWorld().getHandle() != (Object) this) {
            ((WorldBridge) getWorld().getHandle()).bridge$pushAddEntityReason(reason);
        }
    }

    @Override
    public CreatureSpawnEvent.SpawnReason bridge$getAddEntityReason() {
        if (getWorld().getHandle() != (Object) this) {
            return ((WorldBridge) getWorld().getHandle()).bridge$getAddEntityReason();
        }
        return null;
    }

    @Override
    public boolean bridge$preventPoiUpdated() {
        return this.preventPoiUpdated;
    }

    @Override
    public void bridge$preventPoiUpdated(boolean b) {
        this.preventPoiUpdated = b;
    }

    private transient Explosion.BlockInteraction arclight$blockInteractionOverride;

    @ModifyVariable(method = "explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;ZLnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/level/Explosion;",
        ordinal = 0, at = @At("HEAD"), argsOnly = true)
    private Level.ExplosionInteraction arclight$standardExplodePre(Level.ExplosionInteraction interaction) {
        if (interaction == ArclightConstants.STANDARD) {
            arclight$blockInteractionOverride = Explosion.BlockInteraction.DESTROY;
            return Level.ExplosionInteraction.BLOCK;
        }
        return interaction;
    }

    @ModifyVariable(method = "explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;ZLnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/level/Explosion;",
        at = @At(value = "LOAD", ordinal = 0))
    private Explosion.BlockInteraction arclight$standardExplodePost(Explosion.BlockInteraction interaction) {
        try {
            if (arclight$blockInteractionOverride != null) {
                return arclight$blockInteractionOverride;
            } else {
                return interaction;
            }
        } finally {
            arclight$blockInteractionOverride = null;
        }
    }

    // Fusion Core: Paper's BlockDestroyEvent (Paper patch 0274) in destroyBlock, only when something listens, as in Paper.
    // A listener may cancel, change the break effect, hide it, stop the drops or change their experience (handed to the
    // loader's drop handling through FusionCoreBlockExperience; dropped with the items only, as in Paper).

    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z", cancellable = true,
        at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
    private void fusioncore$blockDestroyEvent(BlockPos pos, boolean drop, Entity entity, int recursionLeft, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir,
                                            @com.llamalad7.mixinextras.sugar.Local(ordinal = 0) BlockState state, @com.llamalad7.mixinextras.sugar.Local net.minecraft.world.level.material.FluidState fluid,
                                            @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef dropRef,
                                            @com.llamalad7.mixinextras.sugar.Share("fusioncoreEffectDone") com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef effectDone,
                                            @com.llamalad7.mixinextras.sugar.Share("fusioncoreExpectedExp") com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef expectedExp) {
        if (this.isClientSide || com.destroystokyo.paper.event.block.BlockDestroyEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }
        net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) (Object) this;
        int experience = io.izzel.arclight.common.mod.util.FusionCoreBlockExperience.defaultExp(state, level, pos,
            state.hasBlockEntity() ? level.getBlockEntity(pos) : null, entity, net.minecraft.world.item.ItemStack.EMPTY);
        com.destroystokyo.paper.event.block.BlockDestroyEvent event = new com.destroystokyo.paper.event.block.BlockDestroyEvent(CraftBlock.at((Level) (Object) this, pos),
            CraftBlockData.fromData(fluid.createLegacyBlock()), CraftBlockData.fromData(state), experience, drop);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
            return;
        }
        BlockState effect = ((CraftBlockData) event.getEffectBlock()).getState();
        if (event.playEffect() && !(effect.getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock)) {
            ((Level) (Object) this).levelEvent(2001, pos, Block.getId(effect));
        }
        effectDone.set(true);
        dropRef.set(event.willDrop());
        if (event.willDrop()) {
            io.izzel.arclight.common.mod.util.FusionCoreBlockExperience.expect(level, pos, event.getExpToDrop());
            expectedExp.set(true);
        }
    }

    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"))
    private void fusioncore$blockDestroyExp(BlockPos pos, boolean drop, Entity entity, int recursionLeft, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir,
                                          @com.llamalad7.mixinextras.sugar.Share("fusioncoreExpectedExp") com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef expectedExp) {
        if (expectedExp.get()) {
            io.izzel.arclight.common.mod.util.FusionCoreBlockExperience.finish((net.minecraft.server.level.ServerLevel) (Object) this, pos, true, entity);
        }
    }

    @com.llamalad7.mixinextras.injector.v2.WrapWithCondition(method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
    private boolean fusioncore$destroyEffect(Level level, int type, BlockPos pos, int data,
                                           @com.llamalad7.mixinextras.sugar.Share("fusioncoreEffectDone") com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef effectDone) {
        return !effectDone.get();

    }
}
