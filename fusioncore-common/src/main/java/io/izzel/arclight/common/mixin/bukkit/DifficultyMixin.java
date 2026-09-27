package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code translationKey()} (patch 0404). */
@Mixin(value = org.bukkit.Difficulty.class, remap = false)
public abstract class DifficultyMixin implements net.kyori.adventure.translation.Translatable {

    public String translationKey() {
        return "options.difficulty." + ((org.bukkit.Difficulty) (Object) this).name().toLowerCase(java.util.Locale.ENGLISH);
    }
}
