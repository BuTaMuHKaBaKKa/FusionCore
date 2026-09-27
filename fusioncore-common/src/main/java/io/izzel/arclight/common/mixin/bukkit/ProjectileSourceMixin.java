package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code launchProjectile} with a consumer run before the projectile is added (patch 0674). */
@Mixin(value = org.bukkit.projectiles.ProjectileSource.class, remap = false)
public interface ProjectileSourceMixin {

    default <T extends org.bukkit.entity.Projectile> T launchProjectile(Class<? extends T> projectile, org.bukkit.util.Vector velocity, java.util.function.Consumer<? super T> function) {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.launchProjectile((org.bukkit.projectiles.ProjectileSource) this, projectile, velocity, function);
    }
}
