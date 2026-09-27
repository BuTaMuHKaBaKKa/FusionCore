package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code shouldBurnInDay} for skeletons (patch 0528).
 */
@Mixin(value = org.bukkit.entity.AbstractSkeleton.class, remap = false)
public interface AbstractSkeletonMixin {

    default boolean shouldBurnInDay() {
        return ((io.izzel.arclight.common.bridge.core.world.entity.api.SunBurnBridge) FusionCoreEntities.handle(this)).fusioncore$shouldBurnInDay();
    }

    default void setShouldBurnInDay(boolean shouldBurnInDay) {
        ((io.izzel.arclight.common.bridge.core.world.entity.api.SunBurnBridge) FusionCoreEntities.handle(this)).fusioncore$setShouldBurnInDay(shouldBurnInDay);
    }
}
