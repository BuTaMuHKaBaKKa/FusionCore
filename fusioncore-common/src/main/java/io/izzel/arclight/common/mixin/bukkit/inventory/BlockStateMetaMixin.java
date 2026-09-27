package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code clearBlockState} (patch 0377). The only implementations (CraftMetaBlockState, CraftMetaShield) override these through
 * {@code CraftMetaBlockStateMixin} and {@code CraftMetaShieldMixin}; the defaults only exist so that the plugin's {@code INVOKEINTERFACE} resolves.
 */
@Mixin(value = org.bukkit.inventory.meta.BlockStateMeta.class, remap = false)
public interface BlockStateMetaMixin {

    default void clearBlockState() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }
}
