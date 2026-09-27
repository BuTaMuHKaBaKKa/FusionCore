package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's wololo target (patch 0403).
 */
@Mixin(value = org.bukkit.entity.Evoker.class, remap = false)
public interface EvokerMixin {

    default org.bukkit.entity.Sheep getWololoTarget() {
        net.minecraft.world.entity.animal.Sheep sheep = ((io.izzel.arclight.common.mixin.core.world.entity.api.EvokerApiInvoker) FusionCoreEntities.handle(this)).fusioncore$getWololoTarget();
        return sheep == null ? null : (org.bukkit.entity.Sheep) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) sheep).bridge$getBukkitEntity();
    }

    default void setWololoTarget(org.bukkit.entity.Sheep sheep) {
        ((io.izzel.arclight.common.mixin.core.world.entity.api.EvokerApiInvoker) FusionCoreEntities.handle(this)).fusioncore$setWololoTarget(sheep == null ? null : FusionCoreEntities.nms(sheep));
    }
}
