package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's pollination counters (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Bee.class, remap = false)
public interface BeeMixin {

    default void setRollingOverride(net.kyori.adventure.util.TriState rolling) {
        ((io.izzel.arclight.common.bridge.core.world.entity.api.BeeApiBridge) FusionCoreEntities.handle(this)).fusioncore$setRollingOverride(rolling);
    }

    default boolean isRolling() {
        io.izzel.arclight.common.bridge.core.world.entity.api.BeeApiBridge bee = (io.izzel.arclight.common.bridge.core.world.entity.api.BeeApiBridge) FusionCoreEntities.handle(this);
        return bee.fusioncore$rollingOverride().toBooleanOrElse(bee.fusioncore$isRolling());
    }

    default net.kyori.adventure.util.TriState getRollingOverride() {
        return ((io.izzel.arclight.common.bridge.core.world.entity.api.BeeApiBridge) FusionCoreEntities.handle(this)).fusioncore$rollingOverride();
    }

    default void setCropsGrownSincePollination(int crops) {
        net.minecraft.world.entity.animal.Bee h = FusionCoreEntities.nms(this);
        h.numCropsGrownSincePollination = crops;
    }

    default int getCropsGrownSincePollination() {
        net.minecraft.world.entity.animal.Bee h = FusionCoreEntities.nms(this);
        return h.numCropsGrownSincePollination;
    }

    default void setTicksSincePollination(int ticks) {
        net.minecraft.world.entity.animal.Bee h = FusionCoreEntities.nms(this);
        h.ticksWithoutNectarSinceExitingHive = ticks;
    }

    default int getTicksSincePollination() {
        net.minecraft.world.entity.animal.Bee h = FusionCoreEntities.nms(this);
        return h.ticksWithoutNectarSinceExitingHive;
    }
}
