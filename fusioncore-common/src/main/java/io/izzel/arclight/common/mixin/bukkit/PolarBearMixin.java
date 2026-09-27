package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's standing polar bear (patch 0557).
 */
@Mixin(value = org.bukkit.entity.PolarBear.class, remap = false)
public interface PolarBearMixin {

    default boolean isStanding() {
        net.minecraft.world.entity.animal.PolarBear h = FusionCoreEntities.nms(this);
        return h.isStanding();
    }

    default void setStanding(boolean standing) {
        net.minecraft.world.entity.animal.PolarBear h = FusionCoreEntities.nms(this);
        h.setStanding(standing);
    }
}
