package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's fox API (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Fox.class, remap = false)
public interface FoxMixin {

    default void setInterested(boolean interested) {
        net.minecraft.world.entity.animal.Fox h = FusionCoreEntities.nms(this);
        h.setIsInterested(interested);
    }

    default boolean isInterested() {
        net.minecraft.world.entity.animal.Fox h = FusionCoreEntities.nms(this);
        return h.isInterested();
    }

    default void setLeaping(boolean leaping) {
        net.minecraft.world.entity.animal.Fox h = FusionCoreEntities.nms(this);
        h.setIsPouncing(leaping);
    }

    default boolean isLeaping() {
        net.minecraft.world.entity.animal.Fox h = FusionCoreEntities.nms(this);
        return h.isPouncing();
    }

    default void setDefending(boolean defending) {
        ((io.izzel.arclight.common.mixin.core.world.entity.api.FoxApiInvoker) FusionCoreEntities.handle(this)).fusioncore$setDefending(defending);
    }

    default boolean isDefending() {
        return ((io.izzel.arclight.common.mixin.core.world.entity.api.FoxApiInvoker) FusionCoreEntities.handle(this)).fusioncore$isDefending();
    }

    default void setFaceplanted(boolean faceplanted) {
        ((io.izzel.arclight.common.mixin.core.world.entity.api.FoxApiInvoker) FusionCoreEntities.handle(this)).fusioncore$setFaceplanted(faceplanted);
    }
}
