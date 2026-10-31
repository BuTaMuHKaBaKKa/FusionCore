package io.izzel.arclight.common.mixin.optimization.general.activationrange.entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

// Fusion Core: Paper's Entity Activation Range 2.0, an inactive mob still runs its goal and target selectors every third tick
@Mixin(Mob.class)
public abstract class MobMixin_ActivationRange extends LivingEntityMixin_ActivationRange {

    // @formatter:off
    @Shadow @Final public GoalSelector goalSelector;
    @Shadow @Final public GoalSelector targetSelector;
    // @formatter:on

    @Unique private int fusioncore$inactiveGoalTicks;

    @Override
    public void inactiveTick() {
        super.inactiveTick();
        if (++this.fusioncore$inactiveGoalTicks % 3 == 0) {
            this.goalSelector.tick();
            this.targetSelector.tick();
        }
    }
}
