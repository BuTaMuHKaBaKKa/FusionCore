package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's chicken API (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Chicken.class, remap = false)
public interface ChickenMixin {

    default boolean isChickenJockey() {
        net.minecraft.world.entity.animal.Chicken h = FusionCoreEntities.nms(this);
        return h.isChickenJockey();
    }

    default void setIsChickenJockey(boolean isChickenJockey) {
        net.minecraft.world.entity.animal.Chicken h = FusionCoreEntities.nms(this);
        h.setChickenJockey(isChickenJockey);
    }

    default int getEggLayTime() {
        net.minecraft.world.entity.animal.Chicken h = FusionCoreEntities.nms(this);
        return h.eggTime;
    }

    default void setEggLayTime(int eggLayTime) {
        net.minecraft.world.entity.animal.Chicken h = FusionCoreEntities.nms(this);
        h.eggTime = eggLayTime;
    }
}
