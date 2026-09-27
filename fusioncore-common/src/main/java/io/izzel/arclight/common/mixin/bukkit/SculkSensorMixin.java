package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.bukkit.block.SculkSensor;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's configurable listener range on {@link SculkSensor} (patch 0665), on the snapshot. */
@Mixin(value = SculkSensor.class, remap = false)
public interface SculkSensorMixin {

    default int getListenerRange() {
        return FusionCoreBlockStates.listenerRange(this);
    }

    default void setListenerRange(int range) {
        FusionCoreBlockStates.setListenerRange(this, range);
    }
}
