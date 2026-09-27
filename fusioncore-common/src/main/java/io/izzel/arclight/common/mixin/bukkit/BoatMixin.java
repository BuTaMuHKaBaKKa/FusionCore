package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code getBoatMaterial} (patch 0452), {@link io.papermc.paper.entity.Leashable} (patch 1042).
 */
@Mixin(value = org.bukkit.entity.Boat.class, remap = false)
public interface BoatMixin extends io.papermc.paper.entity.Leashable {

    default org.bukkit.Material getBoatMaterial() {
        net.minecraft.world.entity.vehicle.Boat h = FusionCoreEntities.nms(this);
        return org.bukkit.craftbukkit.v.util.CraftMagicNumbers.getMaterial(h.getDropItem());
    }

    @Override
    default boolean isLeashed() {
        return FusionCoreEntities.isLeashed(this);
    }

    @Override
    default org.bukkit.entity.Entity getLeashHolder() throws IllegalStateException {
        return FusionCoreEntities.leashHolder(this);
    }

    @Override
    default boolean setLeashHolder(org.bukkit.entity.Entity holder) {
        return FusionCoreEntities.setLeashHolder(this, holder);
    }
}
