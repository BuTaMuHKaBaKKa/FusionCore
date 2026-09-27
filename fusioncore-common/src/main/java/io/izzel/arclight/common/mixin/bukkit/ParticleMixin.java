package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Particle#builder()}. */
@Mixin(value = org.bukkit.Particle.class, remap = false)
public abstract class ParticleMixin {

    public com.destroystokyo.paper.ParticleBuilder builder() {
        return new com.destroystokyo.paper.ParticleBuilder((org.bukkit.Particle) (Object) this);
    }
}
