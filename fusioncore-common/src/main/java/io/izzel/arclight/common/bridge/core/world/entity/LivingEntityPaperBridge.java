package io.izzel.arclight.common.bridge.core.world.entity;

/** Fusion Core: Paper's per-entity shield blocking delay (patch 0199), see {@code LivingEntityPaperMixin}. */
public interface LivingEntityPaperBridge {

    int fusioncore$shieldBlockingDelay();

    void fusioncore$setShieldBlockingDelay(int delay);
}
