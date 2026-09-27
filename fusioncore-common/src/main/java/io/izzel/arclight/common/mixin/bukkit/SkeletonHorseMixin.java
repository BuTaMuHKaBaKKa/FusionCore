package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's skeleton trap horse (patch 0218).
 */
@Mixin(value = org.bukkit.entity.SkeletonHorse.class, remap = false)
public interface SkeletonHorseMixin {

    default boolean isTrap() {
        net.minecraft.world.entity.animal.horse.SkeletonHorse h = FusionCoreEntities.nms(this);
        return h.isTrap();
    }

    default void setTrap(boolean trap) {
        net.minecraft.world.entity.animal.horse.SkeletonHorse h = FusionCoreEntities.nms(this);
        h.setTrap(trap);
    }
}
