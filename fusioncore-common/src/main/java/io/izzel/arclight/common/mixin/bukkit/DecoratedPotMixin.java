package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.block.DecoratedPot;
import org.bukkit.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: {@link DecoratedPot} as a {@link io.papermc.paper.block.TileStateInventoryHolder} (API patch 0488). */
@Mixin(value = DecoratedPot.class, remap = false)
public interface DecoratedPotMixin extends io.papermc.paper.block.TileStateInventoryHolder {

    @Override
    default Inventory getSnapshotInventory() {
        return ((DecoratedPot) (Object) this).getSnapshotInventory();
    }
}
