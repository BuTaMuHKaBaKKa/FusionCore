package io.izzel.arclight.common.mixin.bukkit.event;

import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code EnchantItemEvent#setItem} (API patch 0453, server 0946); the table enchants the item set here. */
@Mixin(value = EnchantItemEvent.class, remap = false)
public abstract class EnchantItemEventMixin {

    @Shadow @Final @Mutable private ItemStack item;

    public void setItem(ItemStack item) {
        this.item = item;
    }
}
