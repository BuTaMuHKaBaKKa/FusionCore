package io.izzel.arclight.common.mixin.core.world.level.block.entity.api;

import io.izzel.arclight.common.bridge.core.world.level.block.entity.BlockEntityPaperStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: the sculk sensor's listener range override (Paper patch 0665), see {@link SculkSensorApiMixin}. */
@Mixin(targets = "net.minecraft.world.level.block.entity.SculkSensorBlockEntity$VibrationUser")
public abstract class SculkSensorUserApiMixin implements BlockEntityPaperStateBridge {

    @Unique private Integer fusioncore$rangeOverride;

    @Override
    public Integer fusioncore$rangeOverride() {
        return this.fusioncore$rangeOverride;
    }

    @Override
    public void fusioncore$setRangeOverride(Integer range) {
        this.fusioncore$rangeOverride = range;
    }

    @Inject(method = "getListenerRadius", cancellable = true, at = @At("HEAD"))
    private void fusioncore$rangeOverride(CallbackInfoReturnable<Integer> cir) {
        if (this.fusioncore$rangeOverride != null) {
            cir.setReturnValue(this.fusioncore$rangeOverride);
        }
    }
}
