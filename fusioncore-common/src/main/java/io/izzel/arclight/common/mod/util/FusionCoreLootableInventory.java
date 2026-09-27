package io.izzel.arclight.common.mod.util;

import com.destroystokyo.paper.loottable.LootableInventory;

import java.util.Objects;
import java.util.UUID;

/** Fusion Core: Paper's {@code PaperLootableInventory} (patch 0095), the bodies of {@link LootableInventory}. */
public interface FusionCoreLootableInventory extends LootableInventory {

    /** The holder's replenish data, or null when it has none. */
    FusionCoreLootables.Data fusioncore$lootData();

    private FusionCoreLootables.Data fusioncore$data() {
        return Objects.requireNonNull(this.fusioncore$lootData(), "Can only manage loot tables on tile entities with lootableData");
    }

    @Override
    default boolean isRefillEnabled() {
        return FusionCoreLootables.autoReplenish();
    }

    @Override
    default boolean hasBeenFilled() {
        return this.getLastFilled() != -1;
    }

    @Override
    default boolean hasPlayerLooted(UUID player) {
        return this.fusioncore$data().hasPlayerLooted(player);
    }

    @Override
    default boolean canPlayerLoot(UUID player) {
        return this.fusioncore$data().canPlayerLoot(player);
    }

    @Override
    default Long getLastLooted(UUID player) {
        return this.fusioncore$data().getLastLooted(player);
    }

    @Override
    default boolean setHasPlayerLooted(UUID player, boolean looted) {
        boolean hasLooted = this.hasPlayerLooted(player);
        if (hasLooted != looted) {
            this.fusioncore$data().setPlayerLootedState(player, looted);
        }
        return hasLooted;
    }

    @Override
    default boolean hasPendingRefill() {
        long nextRefill = this.fusioncore$data().getNextRefill();
        return nextRefill != -1 && nextRefill > this.fusioncore$data().getLastFill();
    }

    @Override
    default long getLastFilled() {
        return this.fusioncore$data().getLastFill();
    }

    @Override
    default long getNextRefill() {
        return this.fusioncore$data().getNextRefill();
    }

    @Override
    default long setNextRefill(long refillAt) {
        if (refillAt < -1) {
            refillAt = -1;
        }
        return this.fusioncore$data().setNextRefill(refillAt);
    }
}
