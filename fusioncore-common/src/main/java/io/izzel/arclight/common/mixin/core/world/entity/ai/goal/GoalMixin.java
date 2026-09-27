package io.izzel.arclight.common.mixin.core.world.entity.ai.goal;

import io.izzel.arclight.common.bridge.core.world.entity.ai.GoalBridge;
import net.minecraft.world.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: holds the goal's Paper API wrapper, so the Mob Goal API hands out the same object every time. */
@Mixin(Goal.class)
public abstract class GoalMixin implements GoalBridge {

    @Unique private Object fusioncore$paperGoal;

    @Override
    public Object bridge$paperGoal() {
        return this.fusioncore$paperGoal;
    }

    @Override
    public void bridge$setPaperGoal(Object goal) {
        this.fusioncore$paperGoal = goal;
    }
}
