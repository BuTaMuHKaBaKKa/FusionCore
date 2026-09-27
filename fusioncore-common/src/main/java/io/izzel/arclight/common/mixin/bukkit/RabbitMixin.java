package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's carrot ticks (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Rabbit.class, remap = false)
public interface RabbitMixin {

    default void setMoreCarrotTicks(int ticks) {
        net.minecraft.world.entity.animal.Rabbit h = FusionCoreEntities.nms(this);
        h.moreCarrotTicks = ticks;
    }

    default int getMoreCarrotTicks() {
        net.minecraft.world.entity.animal.Rabbit h = FusionCoreEntities.nms(this);
        return h.moreCarrotTicks;
    }
}
