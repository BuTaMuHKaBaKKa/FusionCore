package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's horse API (patch 0557): eating grass, rearing, mouth open.
 */
@Mixin(value = org.bukkit.entity.AbstractHorse.class, remap = false)
public interface AbstractHorseMixin {

    default boolean isEatingGrass() {
        net.minecraft.world.entity.animal.horse.AbstractHorse h = FusionCoreEntities.nms(this);
        return h.isEating();
    }

    default void setEatingGrass(boolean eating) {
        net.minecraft.world.entity.animal.horse.AbstractHorse h = FusionCoreEntities.nms(this);
        h.setEating(eating);
    }

    default boolean isRearing() {
        net.minecraft.world.entity.animal.horse.AbstractHorse h = FusionCoreEntities.nms(this);
        return h.isStanding();
    }

    /** Paper's {@code setForceStanding}: the standing flag (32) without the timer vanilla's {@code stand()} starts. */
    default void setRearing(boolean rearing) {
        ((io.izzel.arclight.common.mixin.core.world.entity.api.AbstractHorseApiInvoker) FusionCoreEntities.handle(this)).fusioncore$setFlag(32, rearing);
    }

    /** Paper's {@code isMouthOpen}: the open-mouth flag (64). */
    default boolean isEating() {
        return ((io.izzel.arclight.common.mixin.core.world.entity.api.AbstractHorseApiInvoker) FusionCoreEntities.handle(this)).fusioncore$getFlag(64);
    }

    default void setEating(boolean eating) {
        ((io.izzel.arclight.common.mixin.core.world.entity.api.AbstractHorseApiInvoker) FusionCoreEntities.handle(this)).fusioncore$setFlag(64, eating);
    }
}
