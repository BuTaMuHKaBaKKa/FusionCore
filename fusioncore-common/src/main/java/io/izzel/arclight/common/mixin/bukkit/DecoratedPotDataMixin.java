package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's cracked flag on the decorated pot block data (Paper patch 0666 "Add missing block data API"), read from the block's own property. */
@Mixin(value = org.bukkit.block.data.type.DecoratedPot.class, remap = false)
public interface DecoratedPotDataMixin {

    default boolean isCracked() {
        return FusionCoreBlockStates.flag(this, "cracked");
    }

    default void setCracked(boolean cracked) {
        FusionCoreBlockStates.setFlag(this, "cracked", cracked);
    }
}
