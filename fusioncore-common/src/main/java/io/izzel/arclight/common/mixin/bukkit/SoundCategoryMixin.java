package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.SoundCategory} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.SoundCategory.class, remap = false)
public abstract class SoundCategoryMixin {

    /** Paper: the matching Adventure sound source (the names line up one to one). */
    public net.kyori.adventure.sound.Sound.Source soundSource() {
        return net.kyori.adventure.sound.Sound.Source.valueOf(((org.bukkit.SoundCategory) (Object) this).name());
    }
}
