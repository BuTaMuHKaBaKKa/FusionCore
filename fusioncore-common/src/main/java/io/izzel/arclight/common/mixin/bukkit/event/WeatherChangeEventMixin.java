package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent$Cause;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's weather change cause (patch 0540), passed in through a thread-local. */
@Mixin(value = WeatherChangeEvent.class, remap = false)
public abstract class WeatherChangeEventMixin {

    private WeatherChangeEvent$Cause fusioncore$cause;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fusioncore$takeCause(CallbackInfo ci) {
        this.fusioncore$cause = FusionCoreEvents.takeWeatherEventCause();
    }

    public WeatherChangeEvent$Cause getCause() {
        return this.fusioncore$cause;
    }
}
