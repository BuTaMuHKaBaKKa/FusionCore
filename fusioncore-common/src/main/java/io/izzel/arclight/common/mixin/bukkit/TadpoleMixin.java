package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's tadpole age lock (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Tadpole.class, remap = false)
public interface TadpoleMixin {

    default void setAgeLock(boolean lock) {
        ((io.izzel.arclight.common.bridge.core.world.entity.api.TadpoleApiBridge) FusionCoreEntities.handle(this)).fusioncore$setAgeLocked(lock);
    }

    default boolean getAgeLock() {
        return ((io.izzel.arclight.common.bridge.core.world.entity.api.TadpoleApiBridge) FusionCoreEntities.handle(this)).fusioncore$ageLocked();
    }
}
