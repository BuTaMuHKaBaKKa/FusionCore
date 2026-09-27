package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code translationKey()} of a goat horn instrument (patch 0404). */
@Mixin(value = org.bukkit.MusicInstrument.class, remap = false)
public abstract class MusicInstrumentMixin implements net.kyori.adventure.translation.Translatable {

    public String translationKey() {
        return "instrument.minecraft." + ((org.bukkit.MusicInstrument) (Object) this).getKey().getKey();
    }
}
