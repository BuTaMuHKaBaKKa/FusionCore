package io.izzel.arclight.common.mod.server.entity.ai;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import org.bukkit.entity.Mob;

/** Fusion Core: Paper's {@code PaperCustomGoal} (patch 0353). Goals without types run alongside everything. */
public class PaperCustomGoal<T extends Mob> extends net.minecraft.world.entity.ai.goal.Goal {

    private final Goal<T> handle;

    public PaperCustomGoal(Goal<T> handle) {
        this.handle = handle;
        this.setFlags(MobGoalHelper.paperToVanilla(handle.getTypes()));
    }

    @Override
    public boolean canUse() {
        return handle.shouldActivate();
    }

    @Override
    public boolean canContinueToUse() {
        return handle.shouldStayActive();
    }

    @Override
    public void start() {
        handle.start();
    }

    @Override
    public void stop() {
        handle.stop();
    }

    @Override
    public void tick() {
        handle.tick();
    }

    public Goal<T> getHandle() {
        return handle;
    }

    public GoalKey<T> getKey() {
        return handle.getKey();
    }
}
