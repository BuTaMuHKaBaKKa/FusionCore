package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Attributable#registerAttribute} (patch 0489). */
@Mixin(value = org.bukkit.attribute.Attributable.class, remap = false)
public interface AttributableMixin {

    default void registerAttribute(org.bukkit.attribute.Attribute attribute) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.registerAttribute((org.bukkit.attribute.Attributable) this, attribute);
    }
}
