package io.izzel.arclight.common.mixin.bukkit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v.projectiles.CraftBlockProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fusion Core: the consumer of Paper's {@code launchProjectile(type, velocity, consumer)} (patch 0674) runs on the projectile
 * right before the Craft launchers add it to the world, as in Paper.
 */
@Mixin(value = {CraftLivingEntity.class, CraftBlockProjectileSource.class}, remap = false)
public abstract class CraftProjectileLaunchMixin {

    @WrapOperation(method = "launchProjectile(Ljava/lang/Class;Lorg/bukkit/util/Vector;)Lorg/bukkit/entity/Projectile;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean fusioncore$launchConsumer(Level level, Entity projectile, Operation<Boolean> original) {
        FusionCoreEntities.beforeLaunchAdded(projectile);
        return original.call(level, projectile);
    }
}
