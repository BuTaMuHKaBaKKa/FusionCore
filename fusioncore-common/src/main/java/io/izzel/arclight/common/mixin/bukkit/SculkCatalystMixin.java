package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.bukkit.block.SculkCatalyst;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code SculkCatalyst#bloom(Position, int)} (patch 0855). */
@Mixin(value = SculkCatalyst.class, remap = false)
public interface SculkCatalystMixin {

    default void bloom(io.papermc.paper.math.Position position, int charge) {
        FusionCoreBlockStates.bloom(this, position, charge);
    }
}
