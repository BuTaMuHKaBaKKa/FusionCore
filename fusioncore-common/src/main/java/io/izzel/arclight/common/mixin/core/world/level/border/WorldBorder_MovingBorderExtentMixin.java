package io.izzel.arclight.common.mixin.core.world.level.border;

import io.izzel.arclight.common.bridge.core.world.level.border.WorldBorderBridge;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: Paper's WorldBorderBoundsChangeFinishEvent (Paper patch 0495) when a moving border has arrived. */
@Mixin(targets = "net.minecraft.world.level.border.WorldBorder$MovingBorderExtent")
public abstract class WorldBorder_MovingBorderExtentMixin {

    // @formatter:off
    @Shadow(aliases = {"this$0", "field_12743"}) @Final private WorldBorder outerThis;
    @Shadow @Final private double from;
    @Shadow @Final private double to;
    @Shadow @Final private double lerpDuration;
    @Shadow public abstract long getLerpRemainingTime();
    // @formatter:on

    @Inject(method = "update", at = @At("HEAD"))
    private void fusioncore$moveFinished(CallbackInfoReturnable<?> cir) {
        if (this.getLerpRemainingTime() <= 0L) {
            ((WorldBorderBridge) this.outerThis).bridge$borderMoveFinished(this.from, this.to, this.lerpDuration);
        }
    }
}
