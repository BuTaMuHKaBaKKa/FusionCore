package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's pickup cooldown (patch 0557); hopper minecarts have none, as in Paper.
 */
@Mixin(value = org.bukkit.entity.minecart.HopperMinecart.class, remap = false)
public interface HopperMinecartMixin extends com.destroystokyo.paper.loottable.LootableEntityInventory {

    /** Paper's {@code LootableEntityInventory#getEntity} (patch 0039); the other bodies are on {@code CraftMinecartContainer}. */
    @Override
    default org.bukkit.entity.Entity getEntity() {
        return (org.bukkit.entity.Entity) (Object) this;
    }

    default int getPickupCooldown() {
        throw new UnsupportedOperationException("Hopper minecarts don't have cooldowns");
    }

    default void setPickupCooldown(int cooldown) {
        throw new UnsupportedOperationException("Hopper minecarts don't have cooldowns");
    }
}
