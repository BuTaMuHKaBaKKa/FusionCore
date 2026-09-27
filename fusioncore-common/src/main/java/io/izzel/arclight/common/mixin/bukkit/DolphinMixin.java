package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's dolphin API (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Dolphin.class, remap = false)
public interface DolphinMixin {

    default int getMoistness() {
        net.minecraft.world.entity.animal.Dolphin h = FusionCoreEntities.nms(this);
        return h.getMoistnessLevel();
    }

    default void setMoistness(int moistness) {
        net.minecraft.world.entity.animal.Dolphin h = FusionCoreEntities.nms(this);
        h.setMoisntessLevel(moistness);
    }

    default void setHasFish(boolean hasFish) {
        net.minecraft.world.entity.animal.Dolphin h = FusionCoreEntities.nms(this);
        h.setGotFish(hasFish);
    }

    default boolean hasFish() {
        net.minecraft.world.entity.animal.Dolphin h = FusionCoreEntities.nms(this);
        return h.gotFish();
    }

    default org.bukkit.Location getTreasureLocation() {
        net.minecraft.world.entity.animal.Dolphin h = FusionCoreEntities.nms(this);
        return FusionCoreEntities.location(this, h.getTreasurePos());
    }

    default void setTreasureLocation(org.bukkit.Location location) {
        net.minecraft.world.entity.animal.Dolphin h = FusionCoreEntities.nms(this);
        h.setTreasurePos(FusionCoreEntities.blockPos(location));
    }
}
