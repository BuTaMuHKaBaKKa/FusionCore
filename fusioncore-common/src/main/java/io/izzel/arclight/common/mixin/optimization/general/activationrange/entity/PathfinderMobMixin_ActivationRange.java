package io.izzel.arclight.common.mixin.optimization.general.activationrange.entity;

import io.izzel.arclight.common.mod.util.FusionCoreActivation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// Fusion Core: Paper's PathfinderMob#movingTarget, the block a MoveToBlockGoal walks to (read by the bee immunity)
@Mixin(PathfinderMob.class)
public abstract class PathfinderMobMixin_ActivationRange implements FusionCoreActivation.MovingTarget {

    @Unique private BlockPos fusioncore$movingTarget;

    @Override
    public BlockPos fusioncore$getMovingTarget() {
        return this.fusioncore$movingTarget;
    }

    @Override
    public void fusioncore$setMovingTarget(BlockPos pos) {
        this.fusioncore$movingTarget = pos;
    }
}
