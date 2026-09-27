package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code translationKey()} (patch 0404) and {@code isInvulnerable()}. */
@Mixin(value = org.bukkit.GameMode.class, remap = false)
public abstract class GameModeMixin implements net.kyori.adventure.translation.Translatable {

    public String translationKey() {
        return "gameMode." + ((org.bukkit.GameMode) (Object) this).name().toLowerCase(java.util.Locale.ENGLISH);
    }

    /** Paper: creative and spectator players take no damage. */
    public boolean isInvulnerable() {
        org.bukkit.GameMode self = (org.bukkit.GameMode) (Object) this;
        return self == org.bukkit.GameMode.CREATIVE || self == org.bukkit.GameMode.SPECTATOR;
    }
}
