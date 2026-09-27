package io.izzel.arclight.common.bridge.core.world.entity.api;

import net.kyori.adventure.util.TriState;

/** Fusion Core: Paper's bee rolling override (patch 0557); not saved, as in Paper. */
public interface BeeApiBridge {

    TriState fusioncore$rollingOverride();

    /** Sets the override and refreshes the rolling flag, as Paper's {@code CraftBee#setRollingOverride}. */
    void fusioncore$setRollingOverride(TriState rolling);

    boolean fusioncore$isRolling();
}
