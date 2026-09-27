package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.block.ChiseledBookshelf;
import org.bukkit.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: {@link ChiseledBookshelf} as a {@link io.papermc.paper.block.TileStateInventoryHolder} (API patch 0488). */
@Mixin(value = ChiseledBookshelf.class, remap = false)
public interface ChiseledBookshelfMixin extends io.papermc.paper.block.TileStateInventoryHolder {

    @Override
    default Inventory getSnapshotInventory() {
        return ((ChiseledBookshelf) (Object) this).getSnapshotInventory();
    }
}
