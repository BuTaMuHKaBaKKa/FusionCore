package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: the vanilla stack behind a CraftItemStack (package-private {@code handle}; null for an empty stack). */
@Mixin(value = CraftItemStack.class, remap = false)
public interface CraftItemStackAccessor {

    @Accessor("handle")
    net.minecraft.world.item.ItemStack fusioncore$handle();
}
