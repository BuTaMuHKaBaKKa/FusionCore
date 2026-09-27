package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.craftbukkit.v.inventory.CraftInventoryAbstractHorse;
import org.bukkit.inventory.SaddledHorseInventory;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: horse inventories as {@link SaddledHorseInventory}, as in Paper. */
@Mixin(value = CraftInventoryAbstractHorse.class, remap = false)
public abstract class CraftInventoryAbstractHorseMixin implements SaddledHorseInventory {
}
