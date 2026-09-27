package com.destroystokyo.paper.event.inventory;

import org.bukkit.event.inventory.PrepareInventoryResultEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fusion Core: fired after the Bukkit prepare events. Unlike Paper, Bukkit's anvil, grindstone and smithing
 * prepare events do not extend it.
 */
public class PrepareResultEvent extends PrepareInventoryResultEvent {

    // HandlerList on PrepareInventoryResultEvent to ensure api compat

    public PrepareResultEvent(final @NotNull InventoryView inventory, final @Nullable ItemStack result) {
        super(inventory, result);
    }

    /**
     * Get result item, may be {@code null}.
     *
     * @return result item
     */
    @Override
    public @Nullable ItemStack getResult() {
        return super.getResult();
    }

    /**
     * Set result item, may be {@code null}.
     *
     * @param result result item
     */
    @Override
    public void setResult(final @Nullable ItemStack result) {
        super.setResult(result);
    }
}
