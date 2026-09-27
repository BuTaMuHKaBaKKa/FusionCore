package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code PinkPetals#getMinimumFlowerAmount} (Paper patch 0666 "Add missing block data API"), read from the block's own property. */
@Mixin(value = org.bukkit.block.data.type.PinkPetals.class, remap = false)
public interface PinkPetalsMixin {

    default int getMinimumFlowerAmount() {
        return FusionCoreBlockStates.minimum(this, "flower_amount");
    }
}
