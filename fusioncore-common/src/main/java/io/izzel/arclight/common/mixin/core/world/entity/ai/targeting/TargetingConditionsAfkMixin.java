package io.izzel.arclight.common.mixin.core.world.entity.ai.targeting;

import io.izzel.arclight.common.mod.util.FusionCoreAfk;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fusion Core: Purpur's idle-timeout.mobs-target false, an AFK player is no target
@Mixin(TargetingConditions.class)
public abstract class TargetingConditionsAfkMixin {

    @Inject(method = "test", cancellable = true, at = @At("HEAD"))
    private void fusioncore$afk(LivingEntity targeter, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (FusionCoreAfk.hiddenFromMobs(target)) {
            cir.setReturnValue(false);
        }
    }
}
