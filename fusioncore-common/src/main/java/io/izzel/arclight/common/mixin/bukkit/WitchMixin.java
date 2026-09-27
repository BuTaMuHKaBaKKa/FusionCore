package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Witch API (Paper patch 0256 "Add more Witch API"). */
@Mixin(value = org.bukkit.entity.Witch.class, remap = false)
public interface WitchMixin {

    default int getPotionUseTimeLeft() {
        return ((io.izzel.arclight.common.bridge.core.world.entity.monster.WitchBridge) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).bridge$getUsingTime();
    }

    default void setPotionUseTimeLeft(int ticks) {
        ((io.izzel.arclight.common.bridge.core.world.entity.monster.WitchBridge) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).bridge$setUsingTime(ticks);
    }

    default org.bukkit.inventory.ItemStack getDrinkingPotion() {
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(((net.minecraft.world.entity.LivingEntity) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).getMainHandItem());
    }

    default void setDrinkingPotion(org.bukkit.inventory.ItemStack potion) {
        com.google.common.base.Preconditions.checkArgument(potion == null || potion.getType().isAir() || potion.getType() == org.bukkit.Material.POTION, "must be potion, air, or null");
        ((io.izzel.arclight.common.bridge.core.world.entity.monster.WitchBridge) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).bridge$setDrinkingPotion(org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(potion));
    }
}
