package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's input accessors on {@link org.bukkit.inventory.SmithingInventory} (API patch 0185). */
@Mixin(value = org.bukkit.inventory.SmithingInventory.class, remap = false)
public interface SmithingInventoryMixin {

    default org.bukkit.inventory.ItemStack getInputTemplate() {
        return ((org.bukkit.inventory.Inventory) this).getItem(0);
    }

    default void setInputTemplate(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(0, item);
    }

    default org.bukkit.inventory.ItemStack getInputEquipment() {
        return ((org.bukkit.inventory.Inventory) this).getItem(1);
    }

    default void setInputEquipment(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(1, item);
    }

    default org.bukkit.inventory.ItemStack getInputMineral() {
        return ((org.bukkit.inventory.Inventory) this).getItem(2);
    }

    default void setInputMineral(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(2, item);
    }
}
