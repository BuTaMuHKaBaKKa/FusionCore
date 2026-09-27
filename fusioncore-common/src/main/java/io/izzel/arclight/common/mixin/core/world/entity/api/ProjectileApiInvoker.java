package io.izzel.arclight.common.mixin.core.world.entity.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public Projectile methods Paper's entity API calls. */
@Mixin(net.minecraft.world.entity.projectile.Projectile.class)
public interface ProjectileApiInvoker {

    @Invoker("canHitEntity")
    boolean fusioncore$canHitEntity(net.minecraft.world.entity.Entity entity);

    @Invoker("hitTargetOrDeflectSelf")
    net.minecraft.world.entity.projectile.ProjectileDeflection fusioncore$hitTargetOrDeflectSelf(net.minecraft.world.phys.HitResult hit);
}
