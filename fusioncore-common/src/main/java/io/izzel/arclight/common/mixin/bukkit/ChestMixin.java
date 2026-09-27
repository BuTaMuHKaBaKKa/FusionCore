package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v.block.CraftBlockState;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Chest#isBlocked} (patch 0963): vanilla's "cannot open" check, for both halves of a double chest; {@code LootableBlockInventory} (patch 0039, bodies on {@code CraftLootable}). */
@Mixin(value = Chest.class, remap = false)
public interface ChestMixin extends com.destroystokyo.paper.loottable.LootableBlockInventory {

    default boolean isBlocked() {
        return FusionCoreBlockStates.chestBlocked((CraftBlockState) (Object) this);
    }
}
