package io.izzel.arclight.common.mixin.bukkit;

import com.destroystokyo.paper.loottable.LootableBlockInventory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: {@link LootableBlockInventory} on droppers (patch 0039). {@code getBlock} is repeated so Mixin does not treat
 * this as an accessor.
 */
@Mixin(value = org.bukkit.block.Dropper.class, remap = false)
public interface DropperMixin extends LootableBlockInventory {

    @Override
    default Block getBlock() {
        BlockState state = (BlockState) (Object) this;
        return state.getWorld().getBlockAt(state.getX(), state.getY(), state.getZ());
    }
}
