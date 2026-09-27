package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.bukkit.block.Lidded;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Lidded#isOpen} (patch 0541): whether {@code open()} holds the lid open, on the live block entity. */
@Mixin(value = Lidded.class, remap = false)
public interface LiddedMixin {

    default boolean isOpen() {
        return FusionCoreBlockStates.lidOpen(this);
    }
}
