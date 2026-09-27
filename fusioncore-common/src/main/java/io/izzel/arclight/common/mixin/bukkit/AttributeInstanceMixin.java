package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.attribute.AttributeInstance} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.attribute.AttributeInstance.class, remap = false)
public interface AttributeInstanceMixin {

    /** Paper: the modifier with that key, or null. */
    default org.bukkit.attribute.AttributeModifier getModifier(net.kyori.adventure.key.Key key) {
        for (org.bukkit.attribute.AttributeModifier modifier : ((org.bukkit.attribute.AttributeInstance) this).getModifiers()) {
            if (modifier.getKey().getNamespace().equals(key.namespace()) && modifier.getKey().getKey().equals(key.value())) {
                return modifier;
            }
        }
        return null;
    }

    default void removeModifier(net.kyori.adventure.key.Key key) {
        org.bukkit.attribute.AttributeModifier modifier = this.getModifier(key);
        if (modifier != null) {
            ((org.bukkit.attribute.AttributeInstance) this).removeModifier(modifier);
        }
    }

    /**
     * Paper (patch "Add UUID attribute modifier API"): the modifier with the key that replaced this pre-1.21 UUID
     * ({@code FusionCoreAttributeMappings}), or null.
     */
    default org.bukkit.attribute.AttributeModifier getModifier(java.util.UUID uuid) {
        com.google.common.base.Preconditions.checkArgument(uuid != null, "UUID cannot be null");
        return this.getModifier((net.kyori.adventure.key.Key) (Object) io.izzel.arclight.common.mod.util.FusionCoreAttributeMappings.uuidToKey(uuid));
    }

    default void removeModifier(java.util.UUID uuid) {
        com.google.common.base.Preconditions.checkArgument(uuid != null, "UUID cannot be null");
        this.removeModifier((net.kyori.adventure.key.Key) (Object) io.izzel.arclight.common.mod.util.FusionCoreAttributeMappings.uuidToKey(uuid));
    }
}
