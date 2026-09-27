package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Registry#getKey}/{@code getKeyOrThrow}. */
@Mixin(value = org.bukkit.Registry.class, remap = false)
public interface RegistryKeyAccessMixin {

    /** Paper: the key of a registered value. */
    default org.bukkit.NamespacedKey getKey(org.bukkit.Keyed value) {
        return value.getKey();
    }

    default org.bukkit.NamespacedKey getKeyOrThrow(org.bukkit.Keyed value) {
        org.bukkit.NamespacedKey key = this.getKey(value);
        if (key == null) {
            throw new IllegalArgumentException(value + " isn't registered");
        }
        return key;
    }
}
