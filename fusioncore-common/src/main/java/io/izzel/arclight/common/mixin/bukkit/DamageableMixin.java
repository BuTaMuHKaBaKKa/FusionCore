package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Damageable#heal} (patch 0557). */
@Mixin(value = org.bukkit.entity.Damageable.class, remap = false)
public interface DamageableMixin {

    default void heal(double amount) {
        this.heal(amount, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.CUSTOM);
    }

    default void heal(double amount, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason reason) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.heal((org.bukkit.entity.Damageable) this, amount, reason);
    }
}
