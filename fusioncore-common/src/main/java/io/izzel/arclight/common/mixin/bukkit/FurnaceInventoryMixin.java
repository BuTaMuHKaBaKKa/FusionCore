package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreInventories;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code FurnaceInventory#isFuel/canSmelt} (patch 0642). */
@Mixin(value = FurnaceInventory.class, remap = false)
public interface FurnaceInventoryMixin {

    default boolean isFuel(ItemStack stack) {
        return FusionCoreInventories.isFuel(stack);
    }

    default boolean canSmelt(ItemStack stack) {
        return FusionCoreInventories.canSmelt((FurnaceInventory) this, stack);
    }
}
