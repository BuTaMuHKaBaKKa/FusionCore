package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's restocks today (patch 0348), reputation (patch 0354), {@code increaseLevel}/{@code addTrades} (patch 0764).
 */
@Mixin(value = org.bukkit.entity.Villager.class, remap = false)
public interface VillagerMixin {

    default int getRestocksToday() {
        net.minecraft.world.entity.npc.Villager h = FusionCoreEntities.nms(this);
        return h.numberOfRestocksToday;
    }

    default void setRestocksToday(int restocksToday) {
        net.minecraft.world.entity.npc.Villager h = FusionCoreEntities.nms(this);
        h.numberOfRestocksToday = restocksToday;
    }

    default com.destroystokyo.paper.entity.villager.Reputation getReputation(java.util.UUID uniqueId) {
        net.minecraft.world.entity.npc.Villager h = FusionCoreEntities.nms(this);
        net.minecraft.world.entity.ai.gossip.GossipContainer.EntityGossips rep = h.getGossips().gossips.get(uniqueId);
        if (rep == null) {
            return new com.destroystokyo.paper.entity.villager.Reputation(new java.util.EnumMap<>(com.destroystokyo.paper.entity.villager.ReputationType.class));
        }
        return FusionCoreEntities.paperReputation(rep);
    }

    default java.util.Map<java.util.UUID, com.destroystokyo.paper.entity.villager.Reputation> getReputations() {
        net.minecraft.world.entity.npc.Villager h = FusionCoreEntities.nms(this);
        return h.getGossips().gossips.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, entry -> FusionCoreEntities.paperReputation(entry.getValue())));
    }

    default void setReputation(java.util.UUID uniqueId, com.destroystokyo.paper.entity.villager.Reputation reputation) {
        net.minecraft.world.entity.npc.Villager h = FusionCoreEntities.nms(this);
        FusionCoreEntities.assignReputation(h.getGossips().gossips.computeIfAbsent(uniqueId, key -> new net.minecraft.world.entity.ai.gossip.GossipContainer.EntityGossips()), reputation);
    }

    default void setReputations(java.util.Map<java.util.UUID, com.destroystokyo.paper.entity.villager.Reputation> reputations) {
        for (java.util.Map.Entry<java.util.UUID, com.destroystokyo.paper.entity.villager.Reputation> entry : reputations.entrySet()) {
            this.setReputation(entry.getKey(), entry.getValue());
        }
    }

    default void clearReputations() {
        net.minecraft.world.entity.npc.Villager h = FusionCoreEntities.nms(this);
        h.getGossips().gossips.clear();
    }

    default boolean increaseLevel(int amount) {
        com.google.common.base.Preconditions.checkArgument(amount > 0, "Level earned must be positive");
        net.minecraft.world.entity.npc.Villager h = FusionCoreEntities.nms(this);
        int min = net.minecraft.world.entity.npc.VillagerData.MIN_VILLAGER_LEVEL;
        int max = net.minecraft.world.entity.npc.VillagerData.MAX_VILLAGER_LEVEL;
        int supposedFinalLevel = h.getVillagerData().getLevel() + amount;
        com.google.common.base.Preconditions.checkArgument(min <= supposedFinalLevel && supposedFinalLevel <= max,
            "Final level reached after the donation (%d) must be between [%d, %d]".formatted(supposedFinalLevel, min, max));
        it.unimi.dsi.fastutil.ints.Int2ObjectMap<net.minecraft.world.entity.npc.VillagerTrades.ItemListing[]> trades =
            net.minecraft.world.entity.npc.VillagerTrades.TRADES.get(h.getVillagerData().getProfession());
        // Fusion Core: NeoForge fills the table for every profession, with empty levels for those without trades (nitwit, none)
        if (trades == null || trades.values().stream().allMatch(listings -> listings.length == 0)) {
            h.setVillagerData(h.getVillagerData().setLevel(supposedFinalLevel));
            return false;
        }
        while (amount > 0) {
            h.increaseMerchantCareer();
            amount--;
        }
        return true;
    }

    default boolean addTrades(int amount) {
        com.google.common.base.Preconditions.checkArgument(amount > 0, "Number of trades unlocked must be positive");
        return ((io.izzel.arclight.common.bridge.core.world.entity.api.VillagerApiBridge) FusionCoreEntities.nms(this)).fusioncore$updateTrades(amount);
    }
}
