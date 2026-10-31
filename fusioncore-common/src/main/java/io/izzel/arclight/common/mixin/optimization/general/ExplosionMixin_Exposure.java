package io.izzel.arclight.common.mixin.optimization.general;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.izzel.arclight.common.mod.util.FusionCoreExplosions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fusion Core: explosion exposure of huge entities (contraptions, physics constructs) is capped, and kept while one explosion
// runs, see FusionCoreExplosions
@Mixin(Explosion.class)
public abstract class ExplosionMixin_Exposure {

    @WrapMethod(method = "explode")
    private void fusioncore$explosionScope(Operation<Void> original) {
        FusionCoreExplosions.begin();
        try {
            original.call();
        } finally {
            FusionCoreExplosions.end();
        }
    }

    @WrapMethod(method = "getSeenPercent")
    private static float fusioncore$exposure(Vec3 center, Entity entity, Operation<Float> original) {
        return FusionCoreExplosions.seenPercent(center, entity, () -> original.call(center, entity));
    }
}
