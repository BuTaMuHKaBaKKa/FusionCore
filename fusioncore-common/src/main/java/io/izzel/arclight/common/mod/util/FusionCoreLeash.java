package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.bukkit.EntityUnleashEventBridge;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;

/**
 * Fusion Core: Paper's "Expand EntityUnleashEvent" (server patch 0481): unleash events carry whether the leash item drops, and
 * where Paper lets them, cancelling keeps the leash. The callers pass vanilla's drop flag and use the event's.
 */
public final class FusionCoreLeash {

    private FusionCoreLeash() {
    }

    public static EntityUnleashEvent call(Entity entity, EntityUnleashEvent.UnleashReason reason, boolean dropLeash) {
        EntityUnleashEvent event = FusionCoreEvents.newEntityUnleashEvent(((EntityBridge) entity).bridge$getBukkitEntity(), reason, dropLeash);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerUnleashEntityEvent callPlayer(Entity entity, net.minecraft.world.entity.player.Player player, InteractionHand hand, boolean dropLeash) {
        PlayerUnleashEntityEvent event = FusionCoreEvents.newPlayerUnleashEntityEvent(((EntityBridge) entity).bridge$getBukkitEntity(),
            (org.bukkit.entity.Player) ((EntityBridge) player).bridge$getBukkitEntity(), CraftEquipmentSlot.getHand(hand), dropLeash);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean dropLeash(EntityUnleashEvent event) {
        return ((EntityUnleashEventBridge) event).bridge$isDropLeash();
    }

    public static boolean cancelled(EntityUnleashEvent event) {
        return ((EntityUnleashEventBridge) event).bridge$isCancelled();
    }
}
