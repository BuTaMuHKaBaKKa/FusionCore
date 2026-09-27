package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code getPower}/{@code setPower} (patch 0557), Paper's names for the acceleration.
 */
@Mixin(value = org.bukkit.entity.Fireball.class, remap = false)
public interface FireballMixin {

    default void setPower(org.bukkit.util.Vector power) {
        ((org.bukkit.entity.Fireball) this).setAcceleration(power);
    }

    default org.bukkit.util.Vector getPower() {
        return ((org.bukkit.entity.Fireball) this).getAcceleration();
    }
}
