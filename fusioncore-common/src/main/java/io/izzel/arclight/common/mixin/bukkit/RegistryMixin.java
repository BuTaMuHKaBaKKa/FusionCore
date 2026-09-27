package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.Registry} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.Registry.class, remap = false)
public interface RegistryMixin {

    default org.bukkit.Keyed get(net.kyori.adventure.key.Key key) {
        return ((org.bukkit.Registry<?>) this).get((Object) key instanceof org.bukkit.NamespacedKey nk ? nk : new org.bukkit.NamespacedKey(key.namespace(), key.value()));
    }

    default org.bukkit.Keyed getOrThrow(net.kyori.adventure.key.Key key) {
        org.bukkit.Keyed value = this.get(key);
        if (value == null) {
            throw new java.util.NoSuchElementException("No value for " + key + " in " + this);
        }
        return value;
    }

    // Paper patches "improve Registry" and "RegistrySet API"

    default org.bukkit.Keyed get(io.papermc.paper.registry.TypedKey<?> typedKey) {
        com.google.common.base.Preconditions.checkArgument(typedKey != null, "typedKey cannot be null");
        return this.get(typedKey.key());
    }

    default org.bukkit.Keyed getOrThrow(io.papermc.paper.registry.TypedKey<?> typedKey) {
        return this.getOrThrow(typedKey.key());
    }

    /** Paper: registries without tags throw; Bukkit's simple and Craft registries override this. */
    default boolean hasTag(io.papermc.paper.registry.tag.TagKey<?> key) {
        throw new UnsupportedOperationException(this + " doesn't have tags");
    }

    default io.papermc.paper.registry.tag.Tag<?> getTag(io.papermc.paper.registry.tag.TagKey<?> key) {
        throw new UnsupportedOperationException(this + " doesn't have tags");
    }
}
