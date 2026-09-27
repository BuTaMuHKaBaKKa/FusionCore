package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's bat target (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Bat.class, remap = false)
public interface BatMixin {

    default org.bukkit.Location getTargetLocation() {
        net.minecraft.world.entity.ambient.Bat h = FusionCoreEntities.nms(this);
        return FusionCoreEntities.location(this, h.targetPosition);
    }

    default void setTargetLocation(org.bukkit.Location location) {
        net.minecraft.world.entity.ambient.Bat h = FusionCoreEntities.nms(this);
        h.targetPosition = location == null ? null : FusionCoreEntities.blockPos(location);
    }
}
