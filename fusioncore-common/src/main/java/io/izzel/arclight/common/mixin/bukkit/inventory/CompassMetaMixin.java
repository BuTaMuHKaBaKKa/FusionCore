package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's lodestone methods (patch 0960 "General ItemMeta fixes"). The only implementation (CraftMetaCompass) overrides these through
 * {@code CraftMetaCompassMixin}; the defaults only exist so that the plugin's {@code INVOKEINTERFACE} resolves.
 */
@Mixin(value = org.bukkit.inventory.meta.CompassMeta.class, remap = false)
public interface CompassMetaMixin {

    default boolean isLodestoneCompass() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default void clearLodestone() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }
}
