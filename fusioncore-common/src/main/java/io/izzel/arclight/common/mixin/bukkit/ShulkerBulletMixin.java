package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's shulker bullet flight API (patch 0674).
 */
@Mixin(value = org.bukkit.entity.ShulkerBullet.class, remap = false)
public interface ShulkerBulletMixin {

    default org.bukkit.util.Vector getTargetDelta() {
        net.minecraft.world.entity.projectile.ShulkerBullet h = FusionCoreEntities.nms(this);
        return new org.bukkit.util.Vector(h.targetDeltaX, h.targetDeltaY, h.targetDeltaZ);
    }

    default void setTargetDelta(org.bukkit.util.Vector vector) {
        net.minecraft.world.entity.projectile.ShulkerBullet h = FusionCoreEntities.nms(this);
        h.targetDeltaX = vector.getX();
        h.targetDeltaY = vector.getY();
        h.targetDeltaZ = vector.getZ();
    }

    default org.bukkit.block.BlockFace getCurrentMovementDirection() {
        net.minecraft.world.entity.projectile.ShulkerBullet h = FusionCoreEntities.nms(this);
        return h.currentMoveDirection == null ? null : org.bukkit.craftbukkit.v.block.CraftBlock.notchToBlockFace(h.currentMoveDirection);
    }

    default void setCurrentMovementDirection(org.bukkit.block.BlockFace movementDirection) {
        net.minecraft.world.entity.projectile.ShulkerBullet h = FusionCoreEntities.nms(this);
        h.currentMoveDirection = org.bukkit.craftbukkit.v.block.CraftBlock.blockFaceToNotch(movementDirection);
    }

    default int getFlightSteps() {
        net.minecraft.world.entity.projectile.ShulkerBullet h = FusionCoreEntities.nms(this);
        return h.flightSteps;
    }

    default void setFlightSteps(int steps) {
        net.minecraft.world.entity.projectile.ShulkerBullet h = FusionCoreEntities.nms(this);
        h.flightSteps = steps;
    }
}
