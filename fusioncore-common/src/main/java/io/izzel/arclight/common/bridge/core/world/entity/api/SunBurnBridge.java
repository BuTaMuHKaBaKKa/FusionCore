package io.izzel.arclight.common.bridge.core.world.entity.api;

/** Fusion Core: Paper's {@code shouldBurnInDay} on zombies, skeletons and phantoms (patches 0268, 0528), saved as {@code Paper.ShouldBurnInDay}. */
public interface SunBurnBridge {

    boolean fusioncore$shouldBurnInDay();

    void fusioncore$setShouldBurnInDay(boolean shouldBurnInDay);
}
