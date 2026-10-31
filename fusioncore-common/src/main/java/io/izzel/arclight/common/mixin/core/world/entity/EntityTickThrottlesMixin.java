package io.izzel.arclight.common.mixin.core.world.entity;

import io.izzel.arclight.common.mod.util.FusionCoreTickThrottles;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// Fusion Core: per-entity state of the opt-in tick throttles (DAB priority, brain counter, villager lobotomy)
@Mixin(Entity.class)
public abstract class EntityTickThrottlesMixin implements FusionCoreTickThrottles.EntityState {

    @Unique private int fusioncore$activatedPriority = 1;
    @Unique private int fusioncore$priorityTick;
    @Unique private int fusioncore$behaviorTick;
    @Unique private boolean fusioncore$lobotomized;
    @Unique private int fusioncore$notLobotomizedCount;

    @Override
    public int fusioncore$getActivatedPriority() {
        return this.fusioncore$activatedPriority;
    }

    @Override
    public void fusioncore$setActivatedPriority(int priority) {
        this.fusioncore$activatedPriority = priority;
    }

    @Override
    public int fusioncore$getPriorityTick() {
        return this.fusioncore$priorityTick;
    }

    @Override
    public void fusioncore$setPriorityTick(int tick) {
        this.fusioncore$priorityTick = tick;
    }

    @Override
    public int fusioncore$nextBehaviorTick() {
        return this.fusioncore$behaviorTick++;
    }

    @Override
    public boolean fusioncore$isLobotomized() {
        return this.fusioncore$lobotomized;
    }

    @Override
    public void fusioncore$setLobotomized(boolean lobotomized) {
        this.fusioncore$lobotomized = lobotomized;
    }

    @Override
    public int fusioncore$getNotLobotomizedCount() {
        return this.fusioncore$notLobotomizedCount;
    }

    @Override
    public void fusioncore$setNotLobotomizedCount(int count) {
        this.fusioncore$notLobotomizedCount = count;
    }
}
