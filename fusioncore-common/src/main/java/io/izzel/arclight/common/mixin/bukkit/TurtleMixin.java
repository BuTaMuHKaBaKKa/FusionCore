package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's turtle API (patch 0254).
 */
@Mixin(value = org.bukkit.entity.Turtle.class, remap = false)
public interface TurtleMixin {

    default org.bukkit.Location getHome() {
        return FusionCoreEntities.location(this, ((io.izzel.arclight.common.mixin.core.world.entity.api.TurtleApiInvoker) FusionCoreEntities.handle(this)).fusioncore$getHomePos());
    }

    default void setHome(org.bukkit.Location location) {
        net.minecraft.world.entity.animal.Turtle h = FusionCoreEntities.nms(this);
        h.setHomePos(FusionCoreEntities.blockPos(location));
    }

    default boolean isGoingHome() {
        return ((io.izzel.arclight.common.mixin.core.world.entity.api.TurtleApiInvoker) FusionCoreEntities.handle(this)).fusioncore$isGoingHome();
    }

    default boolean isDigging() {
        net.minecraft.world.entity.animal.Turtle h = FusionCoreEntities.nms(this);
        return h.isLayingEgg();
    }

    default void setHasEgg(boolean hasEgg) {
        ((io.izzel.arclight.common.mixin.core.world.entity.api.TurtleApiInvoker) FusionCoreEntities.handle(this)).fusioncore$setHasEgg(hasEgg);
    }
}
