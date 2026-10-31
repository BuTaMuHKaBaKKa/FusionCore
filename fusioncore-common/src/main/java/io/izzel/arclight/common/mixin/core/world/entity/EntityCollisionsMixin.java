package io.izzel.arclight.common.mixin.core.world.entity;

import io.izzel.arclight.common.mod.util.FusionCoreCollisions;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// Fusion Core: Paper's per-entity collision counter (collisions.max-entity-collisions)
@Mixin(Entity.class)
public abstract class EntityCollisionsMixin implements FusionCoreCollisions.Counter {

    @Unique private int fusioncore$numCollisions;

    @Override
    public int fusioncore$getCollisions() {
        return this.fusioncore$numCollisions;
    }

    @Override
    public void fusioncore$setCollisions(int collisions) {
        this.fusioncore$numCollisions = collisions;
    }
}
