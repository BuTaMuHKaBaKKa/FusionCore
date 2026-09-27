package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code isDyed} (patch 0961 "Expose hasColor to leather armor"). The only implementations (CraftMetaLeatherArmor, CraftMetaColorableArmor) override these through
 * {@code CraftMetaLeatherArmorMixin} and {@code CraftMetaColorableArmorMixin}; the defaults only exist so that the plugin's {@code INVOKEINTERFACE} resolves.
 */
@Mixin(value = org.bukkit.inventory.meta.LeatherArmorMeta.class, remap = false)
public interface LeatherArmorMetaMixin {

    default boolean isDyed() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }
}
