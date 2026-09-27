package io.izzel.arclight.common.mod.server.entity.ai;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import io.izzel.arclight.common.bridge.core.world.entity.ai.GoalBridge;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

/** Fusion Core: Paper's {@code PaperVanillaGoal} (patch 0353): a vanilla or mod goal seen through the API. */
public class PaperVanillaGoal<T extends Mob> implements VanillaGoal<T> {

    private final Goal handle;
    private final GoalKey<T> key;

    private final EnumSet<GoalType> types;

    public PaperVanillaGoal(Goal handle) {
        this.handle = handle;
        this.key = MobGoalHelper.getKey(handle.getClass());
        this.types = MobGoalHelper.vanillaToPaper(handle);
    }

    /** Paper's {@code Goal#asPaperVanillaGoal}: one wrapper per goal, kept on the goal. */
    public static <T extends Mob> com.destroystokyo.paper.entity.ai.Goal<T> of(Goal goal) {
        GoalBridge bridge = (GoalBridge) goal;
        Object wrapper = bridge.bridge$paperGoal();
        if (wrapper == null) {
            wrapper = new PaperVanillaGoal<>(goal);
            bridge.bridge$setPaperGoal(wrapper);
        }
        //noinspection unchecked
        return (com.destroystokyo.paper.entity.ai.Goal<T>) wrapper;
    }

    public Goal getHandle() {
        return handle;
    }

    @Override
    public boolean shouldActivate() {
        return handle.canUse();
    }

    @Override
    public boolean shouldStayActive() {
        return handle.canContinueToUse();
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

    @Override
    public @NotNull GoalKey<T> getKey() {
        return key;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return types;
    }
}
