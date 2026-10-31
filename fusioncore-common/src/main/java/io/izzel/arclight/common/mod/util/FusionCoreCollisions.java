package io.izzel.arclight.common.mod.util;

/**
 * Paper's {@code collisions.max-entity-collisions}: an entity pushes and is pushed at most that many times per tick.
 * The counter lives on every entity ({@code EntityCollisionsMixin}); pushes in {@code LivingEntityCollisionsMixin}.
 */
public final class FusionCoreCollisions {

    private FusionCoreCollisions() {
    }

    public interface Counter {

        int fusioncore$getCollisions();

        void fusioncore$setCollisions(int collisions);
    }

    public static int max(net.minecraft.world.level.Level level) {
        return FusionCoreWorldSettings.spec(level).getCollisions().getMaxEntityCollisions();
    }
}
