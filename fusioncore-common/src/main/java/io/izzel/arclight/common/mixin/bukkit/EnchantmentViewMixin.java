package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.world.inventory.EnchantmentMenu;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryView;
import org.bukkit.inventory.view.EnchantmentView;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code EnchantmentView#setEnchantmentSeed} (patch 1047): the seed the menu rolls its offers with. */
@Mixin(value = EnchantmentView.class, remap = false)
public interface EnchantmentViewMixin {

    default void setEnchantmentSeed(int seed) {
        ((EnchantmentMenu) ((CraftInventoryView<?, ?>) (Object) this).getHandle()).enchantmentSeed.set(seed);
    }
}
