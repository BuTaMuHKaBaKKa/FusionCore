package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Inventory#getHolder(boolean)}. */
@Mixin(value = org.bukkit.inventory.Inventory.class, remap = false)
public interface InventoryMixin {

    /** Paper: Spigot has no live block states, so the holder is the same snapshot either way. */
    default org.bukkit.inventory.InventoryHolder getHolder(boolean useSnapshot) {
        return ((org.bukkit.inventory.Inventory) this).getHolder();
    }

    /** Paper's {@code Inventory#close} (patch 0527): closes it for every viewer, returns how many there were. */
    default int close() {
        java.util.List<org.bukkit.entity.HumanEntity> viewers = new java.util.ArrayList<>(((org.bukkit.inventory.Inventory) this).getViewers());
        viewers.forEach(org.bukkit.entity.HumanEntity::closeInventory);
        return viewers.size();
    }

    /** Paper's {@code Inventory#removeItemAnySlot} (patch 0238): {@code removeItem} over all slots, armor and off hand included. */
    default java.util.HashMap<Integer, org.bukkit.inventory.ItemStack> removeItemAnySlot(org.bukkit.inventory.ItemStack... items) {
        return io.izzel.arclight.common.mod.util.FusionCoreInventories.removeItemAnySlot((org.bukkit.inventory.Inventory) this, items);
    }
}
