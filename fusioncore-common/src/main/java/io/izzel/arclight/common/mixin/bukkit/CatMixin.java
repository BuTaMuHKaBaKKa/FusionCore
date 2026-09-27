package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's cat poses (patch 0557), {@link io.papermc.paper.entity.CollarColorable} (patch 0695).
 */
@Mixin(value = org.bukkit.entity.Cat.class, remap = false)
public interface CatMixin extends io.papermc.paper.entity.CollarColorable {

    default void setLyingDown(boolean lyingDown) {
        net.minecraft.world.entity.animal.Cat h = FusionCoreEntities.nms(this);
        h.setLying(lyingDown);
    }

    default boolean isLyingDown() {
        net.minecraft.world.entity.animal.Cat h = FusionCoreEntities.nms(this);
        return h.isLying();
    }

    default void setHeadUp(boolean headUp) {
        ((io.izzel.arclight.common.mixin.core.world.entity.api.CatApiInvoker) FusionCoreEntities.handle(this)).fusioncore$setRelaxStateOne(headUp);
    }

    default boolean isHeadUp() {
        return ((io.izzel.arclight.common.mixin.core.world.entity.api.CatApiInvoker) FusionCoreEntities.handle(this)).fusioncore$isRelaxStateOne();
    }
}
