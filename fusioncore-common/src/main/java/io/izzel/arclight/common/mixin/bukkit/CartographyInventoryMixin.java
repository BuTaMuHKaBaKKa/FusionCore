package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's result accessors on {@link org.bukkit.inventory.CartographyInventory} (API patch 0185). */
@Mixin(value = org.bukkit.inventory.CartographyInventory.class, remap = false)
public interface CartographyInventoryMixin {

    default org.bukkit.inventory.ItemStack getResult() {
        return ((org.bukkit.inventory.Inventory) this).getItem(2);
    }

    default void setResult(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(2, item);
    }
}
