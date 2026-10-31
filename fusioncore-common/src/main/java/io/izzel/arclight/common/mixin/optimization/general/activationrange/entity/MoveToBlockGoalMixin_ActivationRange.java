package io.izzel.arclight.common.mixin.optimization.general.activationrange.entity;

import io.izzel.arclight.common.mod.util.FusionCoreActivation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fusion Core: Paper's activation range improvements, the mob remembers the block it walks to while the goal runs
@Mixin(MoveToBlockGoal.class)
public abstract class MoveToBlockGoalMixin_ActivationRange {

    // @formatter:off
    @Shadow @Final protected PathfinderMob mob;
    @Shadow protected BlockPos blockPos;
    // @formatter:on

    @Inject(method = "findNearestBlock", at = @At("RETURN"))
    private void fusioncore$movingTarget(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            ((FusionCoreActivation.MovingTarget) this.mob).fusioncore$setMovingTarget(this.blockPos == BlockPos.ZERO ? null : this.blockPos.immutable());
        }
    }

    // Paper overrides stop() to forget the target (Goal#stop is empty, so subclasses calling super.stop() reach this). Paper
    // also resets blockPos to BlockPos.ZERO there; that is left out, it changes how the goal resumes.
    public void stop() {
        ((FusionCoreActivation.MovingTarget) this.mob).fusioncore$setMovingTarget(null);
    }
}
