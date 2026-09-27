package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.bukkit.block.Hopper;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's hopper transfer cooldown (patch 0932), on the snapshot; {@code LootableBlockInventory} (patch 0039, bodies on {@code CraftLootable}). */
@Mixin(value = Hopper.class, remap = false)
public interface HopperMixin extends com.destroystokyo.paper.loottable.LootableBlockInventory {

    default void setTransferCooldown(int cooldown) {
        FusionCoreBlockStates.setTransferCooldown(this, cooldown);
    }

    default int getTransferCooldown() {
        return FusionCoreBlockStates.transferCooldown(this);
    }
}
