package io.izzel.arclight.common.bridge.core.world.entity.item;

/** Fusion Core: Paper's item entity state (patches 0132, 0639), see {@code ItemEntityPaperMixin}. */
public interface ItemEntityPaperBridge {

    boolean fusioncore$canMobPickup();

    void fusioncore$setCanMobPickup(boolean canMobPickup);

    int fusioncore$health();

    void fusioncore$setHealth(int health);
}
