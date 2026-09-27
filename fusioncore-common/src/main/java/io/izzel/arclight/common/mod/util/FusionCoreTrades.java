package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.bridge.core.world.item.trading.MerchantOfferBridge;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

/**
 * Fusion Core: Paper's PlayerTradeEvent and PlayerPurchaseEvent (patch 0923). A shift-click fires the event before
 * vanilla moves anything.
 */
public final class FusionCoreTrades {

    private FusionCoreTrades() {
    }

    private static final ThreadLocal<PlayerPurchaseEvent> PENDING = new ThreadLocal<>();
    private static final ThreadLocal<PlayerPurchaseEvent> TAKING = new ThreadLocal<>();
    private static final ThreadLocal<PlayerPurchaseEvent> CURRENT = new ThreadLocal<>();

    /** Builds and calls the event, or returns null for a merchant Paper has no event for (mods' own merchants). */
    public static @Nullable PlayerPurchaseEvent fire(ServerPlayer player, Merchant merchant, MerchantOffer offer) {
        PlayerPurchaseEvent event;
        if (offer == null) {
            return null;
        }
        org.bukkit.entity.Player bukkit = ((ServerPlayerBridge) player).bridge$getBukkitEntity();
        if (merchant instanceof AbstractVillager villager) {
            event = new PlayerTradeEvent(bukkit, (org.bukkit.entity.AbstractVillager) ((EntityBridge) villager).bridge$getBukkitEntity(),
                ((MerchantOfferBridge) offer).bridge$asBukkit(), true, true);
        } else if (isCustomMerchant(merchant)) {
            event = new PlayerPurchaseEvent(bukkit, ((MerchantOfferBridge) offer).bridge$asBukkit(), false, true);
        } else {
            return null;
        }
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /** Shift-click on the result: the event before anything moves. Returns false when it was cancelled. */
    public static boolean firePending(ServerPlayer player, Merchant merchant, MerchantOffer offer) {
        PlayerPurchaseEvent event = fire(player, merchant, offer);
        if (event != null && event.isCancelled()) {
            ((ServerPlayerBridge) player).bridge$getBukkitEntity().updateInventory();
            return false;
        }
        PENDING.set(event);
        return true;
    }

    public static @Nullable PlayerPurchaseEvent takePending() {
        PlayerPurchaseEvent event = PENDING.get();
        PENDING.remove();
        return event;
    }

    public static void clearPending() {
        PENDING.remove();
    }

    /** The event of the trade the result slot is taking now ({@code onTake}), or null. */
    public static void setTaking(@Nullable PlayerPurchaseEvent event) {
        if (event == null) {
            TAKING.remove();
        } else {
            TAKING.set(event);
        }
    }

    public static @Nullable PlayerPurchaseEvent taking() {
        return TAKING.get();
    }

    /** Runs the merchant's {@code notifyTrade} with the event's choices for uses and experience. */
    public static void notifying(Merchant merchant, MerchantOffer offer, @Nullable PlayerPurchaseEvent event, Runnable notify) {
        if (event == null) {
            notify.run();
            return;
        }
        boolean custom = isCustomMerchant(merchant);
        int uses = offer.getUses();
        PlayerPurchaseEvent previous = CURRENT.get();
        CURRENT.set(event);
        try {
            notify.run();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
        if (custom) {
            if (!event.willIncreaseTradeUses()) {
                ((MerchantOfferBridge) offer).bridge$asBukkit().setUses(uses);
            }
            if (event.isRewardingExp() && merchant.getTradingPlayer() instanceof ServerPlayer player && offer.getXp() > 0) {
                player.level().addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(player.level(), player.getX(), player.getY(), player.getZ(), offer.getXp()));
            }
        }
    }

    private static boolean isCustomMerchant(Merchant merchant) {
        return merchant.getClass().getEnclosingClass() == org.bukkit.craftbukkit.v.inventory.CraftMerchantCustom.class; // its MinecraftMerchant
    }

    /** Whether {@code notifyTrade} should raise the trade's uses (Paper: {@code event == null || willIncreaseTradeUses}). */
    public static boolean increaseUses() {
        PlayerPurchaseEvent event = CURRENT.get();
        return event == null || event.willIncreaseTradeUses();
    }

    /** Whether {@code notifyTrade} should give the trade's experience (Paper: {@code event == null || isRewardingExp}). */
    public static boolean rewardExp() {
        PlayerPurchaseEvent event = CURRENT.get();
        return event == null || event.isRewardingExp();
    }
}
