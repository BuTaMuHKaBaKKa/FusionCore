package io.izzel.arclight.common.mixin.core.world.entity.monster;

import io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fusion Core: Paper's SlimeTargetLivingEntityEvent and {@code Slime#canWander} (Paper patch 0232): a dead target is
 * dropped, and when the goal stops the slime forgets its target, as in Paper.
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Slime$SlimeAttackGoal")
public abstract class Slime_SlimeAttackGoalMixin extends net.minecraft.world.entity.ai.goal.Goal {

    @Shadow @Final private Slime slime;
    @Shadow private int growTiredTimer;

    @com.llamalad7.mixinextras.injector.ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean fusioncore$targetEvent(boolean canUse) {
        return canUse && FusionCorePaperEntityEvents.slimeTarget(this.slime, this.slime.getTarget());
    }

    @com.llamalad7.mixinextras.injector.ModifyReturnValue(method = "canContinueToUse", at = @At("RETURN"))
    private boolean fusioncore$keepTargetEvent(boolean canContinue) {
        return canContinue && FusionCorePaperEntityEvents.slimeTarget(this.slime, this.slime.getTarget());
    }

    @Override
    public void stop() {
        this.growTiredTimer = 0;
        this.slime.setTarget(null);
    }
}
