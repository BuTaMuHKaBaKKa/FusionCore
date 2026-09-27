package io.izzel.arclight.common.bridge.bukkit;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/** Fusion Core: Paper's shear drops on {@code PlayerShearEntityEvent}/{@code BlockShearEntityEvent} for core code. */
public interface ShearDropsBridge {

    List<ItemStack> fusioncore$getDrops();
}
