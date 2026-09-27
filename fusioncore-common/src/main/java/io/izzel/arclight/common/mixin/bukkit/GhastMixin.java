package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's explosion power (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Ghast.class, remap = false)
public interface GhastMixin {

    default int getExplosionPower() {
        net.minecraft.world.entity.monster.Ghast h = FusionCoreEntities.nms(this);
        return h.getExplosionPower();
    }

    default void setExplosionPower(int explosionPower) {
        com.google.common.base.Preconditions.checkArgument(explosionPower >= 0 && explosionPower <= 127, "The explosion power has to be between 0 and 127");
        net.minecraft.world.entity.monster.Ghast h = FusionCoreEntities.nms(this);
        h.explosionPower = explosionPower;
    }
}
