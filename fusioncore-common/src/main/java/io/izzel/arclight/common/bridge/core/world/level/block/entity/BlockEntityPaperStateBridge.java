package io.izzel.arclight.common.bridge.core.world.level.block.entity;

/**
 * Fusion Core: Paper's per-block-entity state behind the block state API. Each block entity mixin implements only
 * the part it owns; the rest keep the defaults and are never called for it.
 */
public interface BlockEntityPaperStateBridge {

    /** Campfire: Paper's {@code CampfireBlockEntity#stopCooking} (patch 0634), one flag per slot. */
    default boolean[] fusioncore$stopCooking() {
        throw new UnsupportedOperationException();
    }

    /** Furnace: Paper's {@code AbstractFurnaceBlockEntity#cookSpeedMultiplier} (patch 0247). */
    default double fusioncore$cookSpeedMultiplier() {
        return 1.0;
    }

    default void fusioncore$setCookSpeedMultiplier(double multiplier) {
        throw new UnsupportedOperationException();
    }

    /** Sculk sensor: Paper's {@code SculkSensorBlockEntity#rangeOverride} (patch 0665), null = the block's own range. */
    default Integer fusioncore$rangeOverride() {
        return null;
    }

    default void fusioncore$setRangeOverride(Integer range) {
        throw new UnsupportedOperationException();
    }
}
