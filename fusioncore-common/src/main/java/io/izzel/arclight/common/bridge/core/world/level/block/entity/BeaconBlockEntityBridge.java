package io.izzel.arclight.common.bridge.core.world.level.block.entity;

import org.bukkit.potion.PotionEffect;

public interface BeaconBlockEntityBridge {

    PotionEffect bridge$getPrimaryEffect();

    PotionEffect bridge$getSecondaryEffect();

    /** Fusion Core: Paper's custom effect range (patch 0429); negative = vanilla's {@code levels * 10 + 10}. */
    double fusioncore$effectRange();

    void fusioncore$setEffectRange(double range);
}
