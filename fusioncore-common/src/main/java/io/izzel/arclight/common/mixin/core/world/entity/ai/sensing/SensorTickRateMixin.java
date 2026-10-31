package io.izzel.arclight.common.mixin.core.world.entity.ai.sensing;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.izzel.arclight.common.mod.util.FusionCoreWorldSettings;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.Sensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Fusion Core: Paper's tick-rates.sensor, ticks between scans of a sensor for an entity type. Keys are the sensor's class
// name in lower case, as on Paper.
@Mixin(Sensor.class)
public abstract class SensorTickRateMixin {

    @ModifyExpressionValue(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/sensing/Sensor;scanRate:I"))
    private int fusioncore$tickRate(int scanRate, @Local(argsOnly = true) LivingEntity entity) {
        return FusionCoreWorldSettings.of(entity.level()).sensorRate(entity.getType(), this, scanRate);
    }
}
