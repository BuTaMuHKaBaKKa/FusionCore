package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's slot accessors on {@link org.bukkit.inventory.AnvilInventory} (API patch 0185). */
@Mixin(value = org.bukkit.inventory.AnvilInventory.class, remap = false)
public interface AnvilInventoryMixin {

    default org.bukkit.inventory.ItemStack getFirstItem() {
        return ((org.bukkit.inventory.Inventory) this).getItem(0);
    }

    default void setFirstItem(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(0, item);
    }

    default org.bukkit.inventory.ItemStack getSecondItem() {
        return ((org.bukkit.inventory.Inventory) this).getItem(1);
    }

    default void setSecondItem(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(1, item);
    }

    default org.bukkit.inventory.ItemStack getResult() {
        return ((org.bukkit.inventory.Inventory) this).getItem(2);
    }

    default void setResult(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(2, item);
    }
}
