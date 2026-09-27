package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code AttributeInstance#addTransientModifier}. */
@Mixin(value = org.bukkit.attribute.AttributeInstance.class, remap = false)
public interface AttributeInstanceTransientMixin {

    /** Paper: a modifier that is not saved with the entity. */
    default void addTransientModifier(org.bukkit.attribute.AttributeModifier modifier) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.addTransientModifier((org.bukkit.attribute.AttributeInstance) this, modifier);
    }
}
