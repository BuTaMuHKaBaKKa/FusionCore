package io.izzel.arclight.common.mixin.core.world.entity.projectile;

import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.HitResult;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonFireball.class)
public abstract class DragonFireballMixin extends ProjectileMixin {

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/DragonFireball;discard()V"))
    private void arclight$hit(HitResult hitResult, CallbackInfo ci) {
        this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.HIT);
    }

    // Fusion Core: Paper's EnderDragonFireballHitEvent (patch 0211): cancelled, no breath cloud and no sound
    @org.spongepowered.asm.mixin.Unique
    private boolean fusioncore$hitCancelled;

    @com.llamalad7.mixinextras.injector.v2.WrapWithCondition(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
    private boolean fusioncore$fireballHit(net.minecraft.world.level.Level level, int type, net.minecraft.core.BlockPos pos, int data,
                                         @com.llamalad7.mixinextras.sugar.Local java.util.List<net.minecraft.world.entity.LivingEntity> targets,
                                         @com.llamalad7.mixinextras.sugar.Local net.minecraft.world.entity.AreaEffectCloud cloud) {
        this.fusioncore$hitCancelled = !io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.dragonFireballHit((net.minecraft.world.entity.Entity) (Object) this, targets, cloud);
        return !this.fusioncore$hitCancelled;
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean fusioncore$fireballCloud(net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity cloud,
                                           com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original) {
        if (this.fusioncore$hitCancelled) {
            this.fusioncore$hitCancelled = false;
            return false;
        }
        return original.call(level, cloud);
    }
}
