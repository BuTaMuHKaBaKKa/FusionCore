package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.world.entity.item.ItemEntityPaperBridge;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's item entity API: mob pickup (patch 0132), no age / no player pickup (patch 0428, the same magic
 * pickup delay and age as Paper, so the state survives a save), health (patch 0639) and friction (patch 0769).
 */
@Mixin(value = org.bukkit.entity.Item.class, remap = false)
public interface ItemMixin extends io.papermc.paper.entity.Frictional {

    default boolean canMobPickup() {
        return ((ItemEntityPaperBridge) io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this)).fusioncore$canMobPickup();
    }

    default void setCanMobPickup(boolean canMobPickup) {
        ((ItemEntityPaperBridge) io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this)).fusioncore$setCanMobPickup(canMobPickup);
    }

    default boolean canPlayerPickup() {
        return ((ItemEntity) io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this)).pickupDelay != Short.MAX_VALUE;
    }

    default void setCanPlayerPickup(boolean canPlayerPickup) {
        ((ItemEntity) io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this)).pickupDelay = canPlayerPickup ? 0 : Short.MAX_VALUE;
    }

    default boolean willAge() {
        return ((ItemEntity) io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this)).age != Short.MIN_VALUE;
    }

    default void setWillAge(boolean willAge) {
        ((ItemEntity) io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this)).age = willAge ? 0 : Short.MIN_VALUE;
    }

    default int getHealth() {
        return ((ItemEntityPaperBridge) io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this)).fusioncore$health();
    }

    default void setHealth(int health) {
        ItemEntity handle = (ItemEntity) io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this);
        if (health <= 0) {
            handle.getItem().onDestroyed(handle);
            ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) handle).bridge$pushEntityRemoveCause(org.bukkit.event.entity.EntityRemoveEvent.Cause.PLUGIN);
            handle.discard();
        } else {
            ((ItemEntityPaperBridge) handle).fusioncore$setHealth(health);
        }
    }

    @Override
    default net.kyori.adventure.util.TriState getFrictionState() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.frictionState(this);
    }

    @Override
    default void setFrictionState(net.kyori.adventure.util.TriState state) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.setFrictionState(this, state);
    }
}
