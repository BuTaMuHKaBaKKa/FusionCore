package io.izzel.arclight.common.mixin.core.world.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge;
import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityPaperBridge;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper's freeze tick lock (patch 0655) and per-entity shield blocking delay (patch 0199). */
@Mixin(LivingEntity.class)
public abstract class LivingEntityPaperMixin implements LivingEntityPaperBridge {

    @Unique private int fusioncore$shieldBlockingDelay = 5;

    @Override
    public int fusioncore$shieldBlockingDelay() {
        return this.fusioncore$shieldBlockingDelay;
    }

    @Override
    public void fusioncore$setShieldBlockingDelay(int delay) {
        this.fusioncore$shieldBlockingDelay = delay;
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setTicksFrozen(I)V"))
    private void fusioncore$freezeLock(LivingEntity self, int ticks, Operation<Void> original) {
        if (!((EntityPaperBridge) self).fusioncore$freezeLocked()) {
            original.call(self, ticks);
        }
    }

    @ModifyExpressionValue(method = "isBlocking", at = @At(value = "CONSTANT", args = "intValue=5"))
    private int fusioncore$shieldBlockingDelay(int vanilla) {
        return this.fusioncore$shieldBlockingDelay;
    }

    // Paper patch 1064: the void damage amount of the world
    @ModifyExpressionValue(method = "onBelowWorld", at = @At(value = "CONSTANT", args = "floatValue=4.0"))
    private float fusioncore$voidDamageAmount(float vanilla) {
        return ((LivingEntity) (Object) this).level() instanceof io.izzel.arclight.common.bridge.core.server.level.ServerLevelPaperBridge world
            ? world.fusioncore$voidDamageAmount() : vanilla;
    }
}
