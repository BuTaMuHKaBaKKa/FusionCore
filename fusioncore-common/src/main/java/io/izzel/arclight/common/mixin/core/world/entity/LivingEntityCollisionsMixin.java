package io.izzel.arclight.common.mixin.core.world.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCoreCollisions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fusion Core: Paper's collisions.max-entity-collisions (Paper patch 0126). Each push counts for both entities; the
// counter drops by the cap every tick the entity pushes, so a crowd costs at most that many pushes per entity.
@Mixin(LivingEntity.class)
public abstract class LivingEntityCollisionsMixin {

    @Inject(method = "pushEntities", at = @At("HEAD"))
    private void fusioncore$decay(CallbackInfo ci) {
        var self = (FusionCoreCollisions.Counter) this;
        self.fusioncore$setCollisions(Math.max(0, self.fusioncore$getCollisions() - FusionCoreCollisions.max(((LivingEntity) (Object) this).level())));
    }

    @WrapOperation(method = "pushEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;doPush(Lnet/minecraft/world/entity/Entity;)V"))
    private void fusioncore$cap(LivingEntity self, Entity other, Operation<Void> original) {
        var counter = (FusionCoreCollisions.Counter) self;
        if (counter.fusioncore$getCollisions() >= FusionCoreCollisions.max(self.level())) {
            return;
        }
        var otherCounter = (FusionCoreCollisions.Counter) other;
        otherCounter.fusioncore$setCollisions(otherCounter.fusioncore$getCollisions() + 1);
        counter.fusioncore$setCollisions(counter.fusioncore$getCollisions() + 1);
        original.call(self, other);
    }
}
