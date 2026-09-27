package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.world.entity.npc.WanderingTraderBridge;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's WanderingTrader API (Paper patches 0523 "Add more WanderingTrader API", 0557 "Missing Entity API"). */
@Mixin(value = org.bukkit.entity.WanderingTrader.class, remap = false)
public interface WanderingTraderMixin {

    default boolean canDrinkPotion() {
        return ((WanderingTraderBridge) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).bridge$canDrinkPotion();
    }

    default void setCanDrinkPotion(boolean bool) {
        ((WanderingTraderBridge) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).bridge$setCanDrinkPotion(bool);
    }

    default boolean canDrinkMilk() {
        return ((WanderingTraderBridge) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).bridge$canDrinkMilk();
    }

    default void setCanDrinkMilk(boolean bool) {
        ((WanderingTraderBridge) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).bridge$setCanDrinkMilk(bool);
    }

    default org.bukkit.Location getWanderingTowards() {
        net.minecraft.world.entity.Entity handle = ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle();
        net.minecraft.core.BlockPos pos = ((WanderingTraderBridge) handle).bridge$getWanderTarget();
        if (pos == null) {
            return null;
        }
        return new org.bukkit.Location(((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) handle.level()).bridge$getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

    default void setWanderingTowards(org.bukkit.Location location) {
        net.minecraft.core.BlockPos pos = location == null ? null : new net.minecraft.core.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ((net.minecraft.world.entity.npc.WanderingTrader) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).setWanderTarget(pos);
    }
}
