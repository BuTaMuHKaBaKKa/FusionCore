package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.inventory.InventoryView} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.inventory.InventoryView.class, remap = false)
public interface InventoryViewMixin {

    /** Spigot keeps the title as legacy text; colours and RGB survive. */
    default net.kyori.adventure.text.Component title() {
        return io.izzel.arclight.common.mod.util.FusionCoreAdventure.fromLegacy(((org.bukkit.inventory.InventoryView) this).getTitle());
    }
}
