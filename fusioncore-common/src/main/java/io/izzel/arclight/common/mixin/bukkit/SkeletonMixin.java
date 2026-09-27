package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code inPowderedSnowTime} (patch 0638).
 */
@Mixin(value = org.bukkit.entity.Skeleton.class, remap = false)
public interface SkeletonMixin {

    default int inPowderedSnowTime() {
        net.minecraft.world.entity.monster.Skeleton h = FusionCoreEntities.nms(this);
        return h.inPowderSnowTime;
    }
}
