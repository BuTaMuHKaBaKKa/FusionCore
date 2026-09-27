package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's slot accessors on {@link org.bukkit.inventory.StonecutterInventory} (API patch 0185). */
@Mixin(value = org.bukkit.inventory.StonecutterInventory.class, remap = false)
public interface StonecutterInventoryMixin {

    default org.bukkit.inventory.ItemStack getInputItem() {
        return ((org.bukkit.inventory.Inventory) this).getItem(0);
    }

    default void setInputItem(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(0, item);
    }

    default org.bukkit.inventory.ItemStack getResult() {
        return ((org.bukkit.inventory.Inventory) this).getItem(1);
    }

    default void setResult(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(1, item);
    }
}
