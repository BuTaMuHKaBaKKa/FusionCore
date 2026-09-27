package io.izzel.arclight.common.mixin.core.world.entity.animal.horse;

import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerLevelBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.SkeletonTrapGoal;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkeletonTrapGoal.class)
public class SkeletonTrapGoalMixin {

    // @formatter:off
    @Shadow @Final private SkeletonHorse horse;
    // @formatter:on

    @Inject(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"),
        slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/SkeletonTrapGoal;createHorse(Lnet/minecraft/world/DifficultyInstance;)Lnet/minecraft/world/entity/animal/horse/AbstractHorse;")))
    private void arclight$thunder(CallbackInfo ci) {
        ((WorldBridge) this.horse.level()).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.TRAP);
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean arclight$addHorse(ServerLevel world, Entity entityIn) {
        ((ServerLevelBridge) world).bridge$strikeLightning((LightningBolt) entityIn, LightningStrikeEvent.Cause.TRAP);
        return true;
    }

    @Inject(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"),
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/SkeletonTrapGoal;createHorse(Lnet/minecraft/world/DifficultyInstance;)Lnet/minecraft/world/entity/animal/horse/AbstractHorse;")))
    private void arclight$jockey(CallbackInfo ci) {
        ((WorldBridge) this.horse.level()).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.JOCKEY);
    }

    /**
     * Fusion Core: Paper's SkeletonHorseTrapEvent (Paper patch 0218) before the trap springs, with the players near the
     * horse (the ones vanilla's check found); cancelled, nothing happens this time.
     */
    @org.spongepowered.asm.mixin.injection.Inject(method = "tick", cancellable = true, at = @At("HEAD"))
    private void fusioncore$trapEvent(CallbackInfo ci) {
        if (com.destroystokyo.paper.event.entity.SkeletonHorseTrapEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }
        java.util.List<org.bukkit.entity.HumanEntity> players = new java.util.ArrayList<>();
        for (net.minecraft.world.entity.player.Player player : this.horse.level().players()) {
            if (io.izzel.arclight.common.mod.util.FusionCorePlayers.affectsSpawning(player)
                && player.distanceToSqr(this.horse.getX(), this.horse.getY(), this.horse.getZ()) < 10.0D * 10.0D) {
                players.add((org.bukkit.entity.HumanEntity) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) player).bridge$getBukkitEntity());
            }
        }
        com.destroystokyo.paper.event.entity.SkeletonHorseTrapEvent event = new com.destroystokyo.paper.event.entity.SkeletonHorseTrapEvent(
            (org.bukkit.entity.SkeletonHorse) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) this.horse).bridge$getBukkitEntity(), players);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    // Fusion Core: Paper's affects-spawning API (Paper patch 0034): the trap springs only for players that affect spawning
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;hasNearbyAlivePlayer(DDDD)Z"))
    private boolean fusioncore$trapPlayers(net.minecraft.world.level.Level level, double x, double y, double z, double range, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.hasNearbyAlivePlayerThatAffectsSpawning(level, x, y, z, range);
    }
}
