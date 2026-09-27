package com.destroystokyo.paper.event.inventory;

import org.bukkit.Warning;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fusion Core: Paper API (1.21.1), same signatures as Paper. The grindstone variant of
 * {@link PrepareResultEvent}.
 */
@Deprecated(since = "1.16.1")
@Warning
public class PrepareGrindstoneEvent extends PrepareResultEvent {

    public PrepareGrindstoneEvent(@NotNull InventoryView inventory, @Nullable ItemStack result) {
        super(inventory, result);
    }

    @NotNull
    @Override
    public GrindstoneInventory getInventory() {
        return (GrindstoneInventory) super.getInventory();
    }

}
