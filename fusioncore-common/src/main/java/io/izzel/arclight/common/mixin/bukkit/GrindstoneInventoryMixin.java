package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's grindstone slot accessors. */
@Mixin(value = org.bukkit.inventory.GrindstoneInventory.class, remap = false)
public interface GrindstoneInventoryMixin {

    default org.bukkit.inventory.ItemStack getUpperItem() {
        return ((org.bukkit.inventory.Inventory) this).getItem(0);
    }

    default void setUpperItem(org.bukkit.inventory.ItemStack upperItem) {
        ((org.bukkit.inventory.Inventory) this).setItem(0, upperItem);
    }

    default org.bukkit.inventory.ItemStack getLowerItem() {
        return ((org.bukkit.inventory.Inventory) this).getItem(1);
    }

    default void setLowerItem(org.bukkit.inventory.ItemStack lowerItem) {
        ((org.bukkit.inventory.Inventory) this).setItem(1, lowerItem);
    }

    default org.bukkit.inventory.ItemStack getResult() {
        return ((org.bukkit.inventory.Inventory) this).getItem(2);
    }

    default void setResult(org.bukkit.inventory.ItemStack result) {
        ((org.bukkit.inventory.Inventory) this).setItem(2, result);
    }
}
