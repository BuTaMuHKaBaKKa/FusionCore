package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code DoubleChest#getLeftSide(boolean)}/{@code getRightSide(boolean)}. */
@Mixin(value = org.bukkit.block.DoubleChest.class, remap = false)
public abstract class DoubleChestMixin {

    @Shadow public abstract org.bukkit.inventory.InventoryHolder getLeftSide();

    @Shadow public abstract org.bukkit.inventory.InventoryHolder getRightSide();

    /** Paper: Spigot has no live block states, so both sides are snapshots either way. */
    public org.bukkit.inventory.InventoryHolder getLeftSide(boolean useSnapshot) {
        return this.getLeftSide();
    }

    public org.bukkit.inventory.InventoryHolder getRightSide(boolean useSnapshot) {
        return this.getRightSide();
    }
}
