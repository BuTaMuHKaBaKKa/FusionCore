package io.izzel.arclight.common.mixin.core.world.level.block.entity.trialspawner;

import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(TrialSpawner.class)
public class TrialSpawnerMixin {

    @Inject(method = "spawnMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"))
    private void arclight$cause(ServerLevel serverLevel, BlockPos blockPos, CallbackInfoReturnable<Optional<UUID>> cir) {
        ((WorldBridge) serverLevel).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER);
    }

    // Fusion Core: Paper patches 0141/0280: a trial spawner's mob is "from a mob spawner", reason TRIAL_SPAWNER
    @org.spongepowered.asm.mixin.injection.ModifyArg(method = "spawnMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"))
    private net.minecraft.world.entity.Entity fusioncore$fromTrialSpawner(net.minecraft.world.entity.Entity entity) {
        ((io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge) entity).fusioncore$setFromMobSpawner(true);
        ((io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge) entity).fusioncore$setSpawnReason(CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER);
        return entity;
    }
}
