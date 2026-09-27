package io.izzel.arclight.common.mixin.core.world.entity.animal;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents;
import net.minecraft.world.entity.animal.Turtle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper's TurtleGoHomeEvent (patch 0254): asked when the goal would start. */
@Mixin(targets = "net.minecraft.world.entity.animal.Turtle$TurtleGoHomeGoal")
public abstract class Turtle_TurtleGoHomeGoalMixin {

    @Shadow @Final private Turtle turtle;

    @ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean fusioncore$goHome(boolean canUse) {
        return canUse && FusionCorePaperMobEvents.turtleGoHome(this.turtle);
    }
}
