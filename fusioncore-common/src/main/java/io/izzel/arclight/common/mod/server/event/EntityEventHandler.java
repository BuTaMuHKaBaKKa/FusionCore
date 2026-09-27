package io.izzel.arclight.common.mod.server.event;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.common.mod.server.world.item.EntityDropContainer;
import io.izzel.arclight.common.bridge.bukkit.EntityDeathEventBridge;
import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityDeathBridge;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.common.mod.util.FusionCoreDeathEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EntityEventHandler {

    /**
     * @return whether the event is cancelled. If so no drop will be spawned in dropAllDeathLoot
     */
    public static boolean monitorLivingDrops(LivingEntity living, DamageSource source, List<ItemEntity> drops, boolean isCancelled) {
        if (!(living instanceof LivingEntityBridge bridge)) {
            return false;
        }
        if (source == null) {
            source = living.damageSources().genericKill();
        }
        if (isCancelled) {
            drops.clear();
        }
        if (living instanceof ServerPlayer player) {
            String dmsgOrig = player.getCombatTracker().getDeathMessage().getString();
            Inventory beforeDeath = ArclightCaptures.getDeathPlayerInv();
            Inventory original;
            if (beforeDeath != null) { // not keeping inventory, from game rule
                original = new Inventory(player);
                original.replaceWith(player.getInventory());
                player.getInventory().replaceWith(beforeDeath);
            } else {
                original = null;
            }
            int expReward = bridge.bridge$getExpReward(source.getEntity());
            final PlayerDeathEvent event;
            try (final var container = new EntityDropContainer()) {
                // Arclight: Spigot drops obtained from getCapturedDrops()
                // Already respect vanilla behaviours by using entity capture
                List<org.bukkit.inventory.ItemStack> loot = container.initDecorate(drops);
                event = ArclightEventFactory.callPlayerDeathEvent(player, source, loot, expReward, dmsgOrig, original == null);
                ((LivingEntityDeathBridge) player).fusioncore$setDeathEvent(event);
                if (FusionCoreDeathEvents.isCancelled(event)) {
                    // Fusion Core: Paper's cancelled death (patch 0242): nothing drops, the player keeps the inventory it had
                    // (restored above for the event) and ServerPlayerMixin stops the death
                    drops.clear();
                    bridge.bridge$setExpToDrop(0);
                    return true;
                }
                FusionCoreDeathEvents.playDeathSound(player, event);
                if (event.getKeepInventory()) {
                    original = null;
                }
                container.convert(loot, drops, bridge::arclight$spawnAtLocationNoAdd);
            }
            // beforeDeath == null : true if we used to keepInventory
            // original == null : true if now we don't keepInventory
            if (beforeDeath != null) {
                if (original == null) {
                    ArclightServer.LOGGER.debug("Overriding keepInventory from false to true. Preserve modified inventory before death.");
                } else {
                    // Don't clear it. Preserve original content.
                    // player.getInventory().clearContent();
                    ((ServerPlayerBridge) player).fusioncore$setSlotsKeptOnDeath(fusioncore$keepItems(event, player.getInventory(), original));
                    player.getInventory().replaceWith(original);
                }
            } else if (!event.getKeepInventory()) {
                ArclightServer.LOGGER.warn("Overriding keepInventory from true to false. This won't take effect.");
            }
            ((ServerPlayerBridge) player).arclight$readDeathEvent(event);
        } else {
            final var extra = ArclightCaptures.consumeExtraDrops();
            if (extra != null) {
                drops.addAll(extra);
            }
            final EntityDeathEvent event;
            try (final var container = new EntityDropContainer()) {
                List<ItemStack> itemStackList = container.initDecorate(drops);
                event = ArclightEventFactory.callEntityDeathEvent(living, source, itemStackList);
                ((LivingEntityDeathBridge) living).fusioncore$setDeathEvent(event);
                if (FusionCoreDeathEvents.isCancelled(event)) {
                    // Fusion Core: Paper's cancelled death (patch 0242): no drops, no experience; LivingEntityDeathMixin revives
                    drops.clear();
                    bridge.bridge$setExpToDrop(0);
                    return true;
                }
                FusionCoreDeathEvents.playDeathSound(living, event);
                container.convert(itemStackList, drops, ((EntityBridge) living)::arclight$spawnAtLocationNoAdd);
            }
            bridge.bridge$setExpToDrop(event.getDroppedExp());
        }
        return drops.isEmpty();
    }

    /** Fusion Core: Paper's {@code PlayerDeathEvent#getItemsToKeep} (patch 0284). Returns the slots written, or null. */
    private static int[] fusioncore$keepItems(PlayerDeathEvent event, Inventory inventory, Inventory after) {
        List<ItemStack> itemsToKeep = ((EntityDeathEventBridge) event).bridge$getItemsToKeep();
        if (itemsToKeep.isEmpty()) {
            return null;
        }
        java.util.List<Integer> slots = new java.util.ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize() && !itemsToKeep.isEmpty(); i++) {
            net.minecraft.world.item.ItemStack item = inventory.getItem(i);
            if (item.isEmpty() || !after.getItem(i).isEmpty()
                || net.minecraft.world.item.enchantment.EnchantmentHelper.has(item, net.minecraft.world.item.enchantment.EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                continue;
            }
            ItemStack bukkitStack = org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(item);
            for (var iterator = itemsToKeep.iterator(); iterator.hasNext(); ) {
                if (bukkitStack.equals(iterator.next())) {
                    iterator.remove();
                    after.setItem(i, item);
                    slots.add(i);
                    break;
                }
            }
        }
        for (ItemStack stack : itemsToKeep) {
            int slot = after.getFreeSlot();
            if (stack == null || slot < 0) {
                continue;
            }
            after.setItem(slot, org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(stack));
            slots.add(slot);
        }
        itemsToKeep.clear();
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }
}
