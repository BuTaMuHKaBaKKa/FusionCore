package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.ThunderChangeEvent$Cause;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's weather change cause (patch 0540), passed in through a thread-local. */
@Mixin(value = ThunderChangeEvent.class, remap = false)
public abstract class ThunderChangeEventMixin {

    private ThunderChangeEvent$Cause fusioncore$cause;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fusioncore$takeCause(CallbackInfo ci) {
        this.fusioncore$cause = FusionCoreEvents.takeThunderEventCause();
    }

    public ThunderChangeEvent$Cause getCause() {
        return this.fusioncore$cause;
    }
}
