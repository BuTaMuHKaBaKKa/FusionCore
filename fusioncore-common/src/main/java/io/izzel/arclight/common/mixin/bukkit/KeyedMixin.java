package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.Keyed} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.Keyed.class, remap = false)
public interface KeyedMixin extends net.kyori.adventure.key.Keyed {

    /** Paper: every Bukkit {@code Keyed} is an Adventure {@code Keyed}; its key is the NamespacedKey (an Adventure Key, see NamespacedKeyMixin). */
    @Override
    default net.kyori.adventure.key.Key key() {
        return (net.kyori.adventure.key.Key) (Object) ((org.bukkit.Keyed) this).getKey();
    }
}
