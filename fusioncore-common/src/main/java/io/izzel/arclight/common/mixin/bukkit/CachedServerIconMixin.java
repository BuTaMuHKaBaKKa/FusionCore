package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.util.CachedServerIcon;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code CachedServerIcon#getData()} and {@code isEmpty()}, so plugins compiled
 * against Paper resolve them. {@code CraftIconCacheMixin} overrides {@code getData()}.
 */
@Mixin(value = CachedServerIcon.class, remap = false)
public interface CachedServerIconMixin {

    default String getData() {
        return null;
    }

    default boolean isEmpty() {
        return ((CachedServerIconMixin) this).getData() == null;
    }
}
