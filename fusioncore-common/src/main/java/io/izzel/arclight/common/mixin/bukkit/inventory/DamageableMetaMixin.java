package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code hasDamageValue}/{@code resetDamage} (patch 0960 "General ItemMeta fixes"). The only implementation (CraftMetaItem) overrides these through
 * {@code CraftMetaItemMixin}; the defaults only exist so that the plugin's {@code INVOKEINTERFACE} resolves.
 */
@Mixin(value = org.bukkit.inventory.meta.Damageable.class, remap = false)
public interface DamageableMetaMixin {

    default boolean hasDamageValue() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default void resetDamage() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }
}
