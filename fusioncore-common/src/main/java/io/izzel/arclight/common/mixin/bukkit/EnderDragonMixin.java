package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's changeable podium (patch 0687).
 */
@Mixin(value = org.bukkit.entity.EnderDragon.class, remap = false)
public interface EnderDragonMixin {

    default org.bukkit.Location getPodium() {
        net.minecraft.core.BlockPos pos = ((io.izzel.arclight.common.bridge.core.world.entity.api.EnderDragonApiBridge) FusionCoreEntities.handle(this)).fusioncore$getPodium();
        return new org.bukkit.Location(((org.bukkit.entity.Entity) this).getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

    default void setPodium(org.bukkit.Location location) {
        io.izzel.arclight.common.bridge.core.world.entity.api.EnderDragonApiBridge dragon = (io.izzel.arclight.common.bridge.core.world.entity.api.EnderDragonApiBridge) FusionCoreEntities.handle(this);
        if (location == null) {
            dragon.fusioncore$setPodium(null);
        } else {
            com.google.common.base.Preconditions.checkArgument(location.getWorld() == null || location.getWorld().equals(((org.bukkit.entity.Entity) this).getWorld()),
                "You cannot set a podium in a different world to where the dragon is");
            dragon.fusioncore$setPodium(FusionCoreEntities.blockPos(location));
        }
    }
}
