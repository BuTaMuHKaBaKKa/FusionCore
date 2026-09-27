package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's ThrownPotion API (Paper patch 0338/0674 "More Projectile API"). */
@Mixin(value = org.bukkit.entity.ThrownPotion.class, remap = false)
public interface ThrownPotionMixin {

    default org.bukkit.inventory.meta.PotionMeta getPotionMeta() {
        return io.izzel.arclight.common.mod.util.FusionCorePotions.getPotionMeta((net.minecraft.world.entity.projectile.ThrownPotion) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle());
    }

    default void setPotionMeta(org.bukkit.inventory.meta.PotionMeta meta) {
        io.izzel.arclight.common.mod.util.FusionCorePotions.setPotionMeta((net.minecraft.world.entity.projectile.ThrownPotion) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle(), meta);
    }

    default void splash() {
        ((io.izzel.arclight.common.bridge.core.world.entity.projectile.ThrownPotionBridge) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).bridge$splash();
    }
}
