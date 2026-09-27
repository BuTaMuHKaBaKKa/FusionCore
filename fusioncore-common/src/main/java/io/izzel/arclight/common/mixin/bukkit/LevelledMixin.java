package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Levelled#getMinimumLevel} (Paper patch 0666 "Add missing block data API"), read from the block's own property. */
@Mixin(value = org.bukkit.block.data.Levelled.class, remap = false)
public interface LevelledMixin {

    default int getMinimumLevel() {
        return FusionCoreBlockStates.minimum(this, "level");
    }
}
