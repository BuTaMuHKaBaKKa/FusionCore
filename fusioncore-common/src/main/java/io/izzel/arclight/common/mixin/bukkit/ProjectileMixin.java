package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's projectile API (patch 0674): shot/left-shooter flags, hitting an entity, owner UUID.
 */
@Mixin(value = org.bukkit.entity.Projectile.class, remap = false)
public interface ProjectileMixin {

    default boolean hasLeftShooter() {
        net.minecraft.world.entity.projectile.Projectile h = FusionCoreEntities.nms(this);
        return h.leftOwner;
    }

    default void setHasLeftShooter(boolean leftShooter) {
        net.minecraft.world.entity.projectile.Projectile h = FusionCoreEntities.nms(this);
        h.leftOwner = leftShooter;
    }

    default boolean hasBeenShot() {
        net.minecraft.world.entity.projectile.Projectile h = FusionCoreEntities.nms(this);
        return h.hasBeenShot;
    }

    default void setHasBeenShot(boolean beenShot) {
        net.minecraft.world.entity.projectile.Projectile h = FusionCoreEntities.nms(this);
        h.hasBeenShot = beenShot;
    }

    default boolean canHitEntity(org.bukkit.entity.Entity entity) {
        return ((io.izzel.arclight.common.mixin.core.world.entity.api.ProjectileApiInvoker) FusionCoreEntities.handle(this)).fusioncore$canHitEntity(FusionCoreEntities.handle(entity));
    }

    default void hitEntity(org.bukkit.entity.Entity entity) {
        ((io.izzel.arclight.common.mixin.core.world.entity.api.ProjectileApiInvoker) FusionCoreEntities.handle(this)).fusioncore$hitTargetOrDeflectSelf(new net.minecraft.world.phys.EntityHitResult(FusionCoreEntities.handle(entity)));
    }

    default void hitEntity(org.bukkit.entity.Entity entity, org.bukkit.util.Vector vector) {
        ((io.izzel.arclight.common.mixin.core.world.entity.api.ProjectileApiInvoker) FusionCoreEntities.handle(this)).fusioncore$hitTargetOrDeflectSelf(new net.minecraft.world.phys.EntityHitResult(FusionCoreEntities.handle(entity),
            new net.minecraft.world.phys.Vec3(vector.getX(), vector.getY(), vector.getZ())));
    }

    default java.util.UUID getOwnerUniqueId() {
        net.minecraft.world.entity.projectile.Projectile h = FusionCoreEntities.nms(this);
        return h.ownerUUID;
    }
}
