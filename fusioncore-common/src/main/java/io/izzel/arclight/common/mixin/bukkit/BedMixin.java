package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Bed#setOccupied} (Paper patch 0666 "Add missing block data API"), read from the block's own property. */
@Mixin(value = org.bukkit.block.data.type.Bed.class, remap = false)
public interface BedMixin {

    default void setOccupied(boolean occupied) {
        FusionCoreBlockStates.setFlag(this, "occupied", occupied);
    }
}
