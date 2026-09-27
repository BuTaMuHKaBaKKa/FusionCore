package io.izzel.arclight.common.bridge.core.world.entity.api;

/** Fusion Core: Paper's {@code Villager#updateTrades(int)} (patch 0764). */
public interface VillagerApiBridge {

    /** Adds up to {@code amount} trades of the villager's current level; false when its profession has none there. */
    boolean fusioncore$updateTrades(int amount);
}
