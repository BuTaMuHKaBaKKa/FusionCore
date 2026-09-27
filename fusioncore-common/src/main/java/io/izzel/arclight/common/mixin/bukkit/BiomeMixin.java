package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code translationKey()} of a biome (patch 0404). */
@Mixin(value = org.bukkit.block.Biome.class, remap = false)
public abstract class BiomeMixin implements net.kyori.adventure.translation.Translatable {

    public String translationKey() {
        return "biome.minecraft." + ((org.bukkit.block.Biome) (Object) this).getKey().getKey();
    }
}
