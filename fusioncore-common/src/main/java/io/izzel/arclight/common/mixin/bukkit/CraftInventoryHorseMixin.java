package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.craftbukkit.v.inventory.CraftInventoryHorse;
import org.bukkit.inventory.ArmoredHorseInventory;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code HorseInventory} is an {@link ArmoredHorseInventory} (API patch 0062); Spigot's getArmor/setArmor implement it. */
@Mixin(value = CraftInventoryHorse.class, remap = false)
public abstract class CraftInventoryHorseMixin implements ArmoredHorseInventory {
}
