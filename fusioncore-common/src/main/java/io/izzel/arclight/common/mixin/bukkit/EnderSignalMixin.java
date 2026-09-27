package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code setTargetLocation(location, update)} (patch 0577): without {@code update} the eye keeps its life and drop decision.
 */
@Mixin(value = org.bukkit.entity.EnderSignal.class, remap = false)
public interface EnderSignalMixin {

    default void setTargetLocation(org.bukkit.Location location, boolean update) {
        com.google.common.base.Preconditions.checkArgument(((org.bukkit.entity.EnderSignal) this).getWorld().equals(location.getWorld()), "Cannot target EnderSignal across worlds");
        net.minecraft.world.entity.projectile.EyeOfEnder h = FusionCoreEntities.nms(this);
        int life = h.life;
        boolean surviveAfterDeath = h.surviveAfterDeath;
        h.signalTo(org.bukkit.craftbukkit.v.util.CraftLocation.toBlockPosition(location));
        if (!update) {
            h.life = life;
            h.surviveAfterDeath = surviveAfterDeath;
        }
    }
}
