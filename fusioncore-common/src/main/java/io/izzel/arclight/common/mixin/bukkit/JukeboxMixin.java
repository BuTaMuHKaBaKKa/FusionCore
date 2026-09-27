package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.block.Jukebox;
import org.bukkit.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: {@link Jukebox} as a {@link io.papermc.paper.block.TileStateInventoryHolder} (API patch 0488). */
@Mixin(value = Jukebox.class, remap = false)
public interface JukeboxMixin extends io.papermc.paper.block.TileStateInventoryHolder {

    @Override
    default Inventory getSnapshotInventory() {
        return ((Jukebox) (Object) this).getSnapshotInventory();
    }
}
