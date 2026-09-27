package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's book accessors on {@link org.bukkit.inventory.LecternInventory} (API patch 0185). */
@Mixin(value = org.bukkit.inventory.LecternInventory.class, remap = false)
public interface LecternInventoryMixin {

    default org.bukkit.inventory.ItemStack getBook() {
        return ((org.bukkit.inventory.Inventory) this).getItem(0);
    }

    default void setBook(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(0, item);
    }
}
