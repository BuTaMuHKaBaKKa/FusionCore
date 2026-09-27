package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.Translatable} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.Translatable.class, remap = false)
public interface TranslatableMixin extends net.kyori.adventure.translation.Translatable {

    /** Paper: Bukkit's translatable types are Adventure translatables with the same key. */
    @Override
    default String translationKey() {
        return ((org.bukkit.Translatable) this).getTranslationKey();
    }
}
