package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's leaf distance bounds (Paper patch 0666 "Add missing block data API"), read from the block's own property. */
@Mixin(value = org.bukkit.block.data.type.Leaves.class, remap = false)
public interface LeavesMixin {

    default int getMinimumDistance() {
        return FusionCoreBlockStates.minimum(this, "distance");
    }

    default int getMaximumDistance() {
        return FusionCoreBlockStates.maximum(this, "distance");
    }
}
