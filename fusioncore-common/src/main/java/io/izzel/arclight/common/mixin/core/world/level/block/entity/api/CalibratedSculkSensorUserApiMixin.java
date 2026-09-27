package io.izzel.arclight.common.mixin.core.world.level.block.entity.api;

import io.izzel.arclight.common.bridge.core.world.level.block.entity.BlockEntityPaperStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: the calibrated sensor overrides the range, so the override (Paper patch 0665) is checked here too. */
@Mixin(targets = "net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity$VibrationUser")
public abstract class CalibratedSculkSensorUserApiMixin {

    @Inject(method = "getListenerRadius", cancellable = true, at = @At("HEAD"))
    private void fusioncore$rangeOverride(CallbackInfoReturnable<Integer> cir) {
        Integer range = ((BlockEntityPaperStateBridge) (Object) this).fusioncore$rangeOverride();
        if (range != null) {
            cir.setReturnValue(range);
        }
    }
}
