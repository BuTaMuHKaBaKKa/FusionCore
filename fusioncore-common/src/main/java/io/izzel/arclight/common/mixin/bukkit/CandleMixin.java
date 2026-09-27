package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Candle#getMinimumCandles} (Paper patch 0666 "Add missing block data API"), read from the block's own property. */
@Mixin(value = org.bukkit.block.data.type.Candle.class, remap = false)
public interface CandleMixin {

    default int getMinimumCandles() {
        return FusionCoreBlockStates.minimum(this, "candles");
    }
}
