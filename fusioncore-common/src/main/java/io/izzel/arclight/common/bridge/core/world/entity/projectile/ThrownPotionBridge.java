package io.izzel.arclight.common.bridge.core.world.entity.projectile;

/** Fusion Core: Paper's {@code ThrownPotion#splash()} (Paper patch 0338 "More Projectile API"). */
public interface ThrownPotionBridge {

    /** Splashes the potion where it is, as if it hit nothing, and removes it. */
    void bridge$splash();
}
