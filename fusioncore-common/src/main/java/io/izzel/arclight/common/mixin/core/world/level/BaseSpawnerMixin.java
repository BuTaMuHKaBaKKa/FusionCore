package io.izzel.arclight.common.mixin.core.world.level;

import io.izzel.arclight.common.bridge.core.world.entity.MobBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.bridge.core.world.level.BaseSpawnerBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftEntityType;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin implements BaseSpawnerBridge {

    // @formatter:off
    @Shadow public SimpleWeightedRandomList<SpawnData> spawnPotentials;
    @Shadow public int spawnCount;
    // @formatter:on

    // Fusion Core: Paper's tick-rates.mob-spawner, a spawner runs every that many ticks and counts its delay down by as
    // much; -1 stops spawners
    @Shadow public int spawnDelay;
    private int fusioncore$tickDelay;

    @Inject(method = "serverTick", cancellable = true, at = @At("HEAD"))
    private void fusioncore$tickRate(net.minecraft.server.level.ServerLevel level, net.minecraft.core.BlockPos pos, CallbackInfo ci) {
        int rate = io.izzel.arclight.common.mod.util.FusionCoreWorldSettings.spec(level).getTickRates().getMobSpawner();
        if (rate == 1) {
            return;
        }
        if (this.spawnDelay > 0 && --this.fusioncore$tickDelay > 0) {
            ci.cancel();
            return;
        }
        this.fusioncore$tickDelay = rate;
        if (rate == -1) {
            ci.cancel();
        }
    }

    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method = "serverTick", at = @At(value = "FIELD", ordinal = 0, opcode = org.objectweb.asm.Opcodes.GETFIELD, target = "Lnet/minecraft/world/level/BaseSpawner;spawnDelay:I"))
    private int fusioncore$resetDelay(int delay, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.server.level.ServerLevel level) {
        int rate = io.izzel.arclight.common.mod.util.FusionCoreWorldSettings.spec(level).getTickRates().getMobSpawner();
        if (rate <= 1) {
            return delay;
        }
        // Paper: with the delay counted down by the rate, any value below -rate means "pick a new delay"
        return delay < -rate ? -1 : (delay == -1 ? 0 : delay);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "serverTick", at = @At(value = "FIELD", opcode = org.objectweb.asm.Opcodes.PUTFIELD, target = "Lnet/minecraft/world/level/BaseSpawner;spawnDelay:I"))
    private void fusioncore$countDown(BaseSpawner spawner, int delay, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
                                      @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.server.level.ServerLevel level) {
        int rate = io.izzel.arclight.common.mod.util.FusionCoreWorldSettings.spec(level).getTickRates().getMobSpawner();
        original.call(spawner, rate > 1 ? delay - (rate - 1) : delay);
    }

    @Inject(method = "setEntityId", at = @At("RETURN"))
    public void arclight$clearMobs(CallbackInfo ci) {
        this.spawnPotentials = SimpleWeightedRandomList.empty();
    }

    @Decorate(method = "serverTick", inject = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/SpawnData;getEquipment()Ljava/util/Optional;"))
    private void arclight$nerf(@Local(ordinal = -1) Mob mob) {
        if (((WorldBridge) mob.level()).bridge$spigotConfig().nerfSpawnerMobs) {
            ((MobBridge) mob).bridge$setAware(false);
        }
    }

    @Decorate(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean arclight$spawnerSpawn(ServerLevel instance, Entity entity, ServerLevel level, BlockPos pos) throws Throwable {
        ((io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge) entity).fusioncore$setFromMobSpawner(true); // Fusion Core: Paper patch 0141
        ((io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge) entity).fusioncore$setSpawnReason(CreatureSpawnEvent.SpawnReason.SPAWNER); // Fusion Core: Paper patch 0280
        if (CraftEventFactory.callSpawnerSpawnEvent(entity, pos).isCancelled()) {
            throw DecorationOps.jumpToLoopStart();
        }
        ((WorldBridge) instance).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.SPAWNER);
        return (boolean) DecorationOps.callsite().invoke(instance, entity);
    }

    // Fusion Core: Paper's PreSpawnerSpawnEvent (Paper patch 0251), fired where Paper fires it: once a spawn attempt passed
    // the spawn rules, before the entity is created. Cancelled: this attempt is skipped and the spawner delay still resets
    // (Paper's flag = true); shouldAbortSpawn also ends the remaining attempts of this tick (Paper's break).

    @ModifyExpressionValue(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/SpawnPlacements;checkSpawnRules(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z"))
    private boolean fusioncore$preSpawnerSpawn(boolean allowed, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) ServerLevel level,
                                             @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) BlockPos pos,
                                             @com.llamalad7.mixinextras.sugar.Local Optional<EntityType<?>> type,
                                             @com.llamalad7.mixinextras.sugar.Local(ordinal = 0) double x,
                                             @com.llamalad7.mixinextras.sugar.Local(ordinal = 1) double y,
                                             @com.llamalad7.mixinextras.sugar.Local(ordinal = 2) double z,
                                             @com.llamalad7.mixinextras.sugar.Local LocalBooleanRef spawned,
                                             @com.llamalad7.mixinextras.sugar.Local(ordinal = 0) LocalIntRef attempt) {
        return allowed && this.fusioncore$firePreSpawnerSpawn(level, pos, type.get(), x, y, z, spawned, attempt);
    }

    @ModifyExpressionValue(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/SpawnData$CustomSpawnRules;isValidPosition(Lnet/minecraft/core/BlockPos;Lnet/minecraft/server/level/ServerLevel;)Z"))
    private boolean fusioncore$preSpawnerSpawnCustomRules(boolean allowed, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) ServerLevel level,
                                                        @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) BlockPos pos,
                                                        @com.llamalad7.mixinextras.sugar.Local Optional<EntityType<?>> type,
                                                        @com.llamalad7.mixinextras.sugar.Local(ordinal = 0) double x,
                                                        @com.llamalad7.mixinextras.sugar.Local(ordinal = 1) double y,
                                                        @com.llamalad7.mixinextras.sugar.Local(ordinal = 2) double z,
                                                        @com.llamalad7.mixinextras.sugar.Local LocalBooleanRef spawned,
                                                        @com.llamalad7.mixinextras.sugar.Local(ordinal = 0) LocalIntRef attempt) {
        return allowed && this.fusioncore$firePreSpawnerSpawn(level, pos, type.get(), x, y, z, spawned, attempt);
    }

    private boolean fusioncore$firePreSpawnerSpawn(ServerLevel level, BlockPos pos, EntityType<?> type, double x, double y, double z,
                                                 LocalBooleanRef spawned, LocalIntRef attempt) {
        if (PreSpawnerSpawnEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return true;
        }
        org.bukkit.World world = ((WorldBridge) level).bridge$getWorld();
        PreSpawnerSpawnEvent event = new PreSpawnerSpawnEvent(new Location(world, x, y, z), CraftEntityType.minecraftToBukkit(type),
            new Location(world, pos.getX(), pos.getY(), pos.getZ()));
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            return true;
        }
        spawned.set(true);
        if (event.shouldAbortSpawn()) {
            attempt.set(this.spawnCount); // ends the loop after this attempt, like Paper's break
        }
        return false;
    }

    // Fusion Core: Paper's affects-spawning API (Paper patch 0034): spawners wake only for players that affect spawning
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "isNearPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;hasNearbyAlivePlayer(DDDD)Z"))
    private boolean fusioncore$spawnerPlayers(net.minecraft.world.level.Level level, double x, double y, double z, double range, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.hasNearbyAlivePlayerThatAffectsSpawning(level, x, y, z, range);
    }
}
