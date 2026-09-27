package io.izzel.arclight.common.mod.server.entity.ai;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.destroystokyo.paper.entity.ai.MobGoals;
import io.izzel.arclight.common.mod.util.FusionCoreMobs;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

/** Fusion Core: Paper's {@code PaperMobGoals} (patch 0353). TARGET goals live in the target selector. */
public class PaperMobGoals implements MobGoals {

    public static final PaperMobGoals INSTANCE = new PaperMobGoals();

    @Override
    public <T extends Mob> void addGoal(@NotNull T mob, int priority, @NotNull Goal<T> goal) {
        net.minecraft.world.entity.ai.goal.Goal mojangGoal;
        if (goal instanceof PaperVanillaGoal<?> vanillaGoal) {
            mojangGoal = vanillaGoal.getHandle();
        } else {
            mojangGoal = new PaperCustomGoal<>(goal);
        }
        getHandle(mob, goal.getTypes()).addGoal(priority, mojangGoal);
    }

    @Override
    public <T extends Mob> void removeGoal(@NotNull T mob, @NotNull Goal<T> goal) {
        if (goal instanceof PaperVanillaGoal<?> vanillaGoal) {
            getHandle(mob, goal.getTypes()).removeGoal(vanillaGoal.getHandle());
        } else {
            List<net.minecraft.world.entity.ai.goal.Goal> toRemove = new LinkedList<>();
            for (WrappedGoal item : getHandle(mob, goal.getTypes()).getAvailableGoals()) {
                if (item.getGoal() instanceof PaperCustomGoal<?> customGoal && customGoal.getHandle() == goal) {
                    toRemove.add(item.getGoal());
                }
            }
            for (net.minecraft.world.entity.ai.goal.Goal g : toRemove) {
                getHandle(mob, goal.getTypes()).removeGoal(g);
            }
        }
    }

    @Override
    public <T extends Mob> void removeAllGoals(@NotNull T mob) {
        for (GoalType type : GoalType.values()) {
            removeAllGoals(mob, type);
        }
    }

    @Override
    public <T extends Mob> void removeAllGoals(@NotNull T mob, @NotNull GoalType type) {
        for (Goal<T> goal : getAllGoals(mob, type)) {
            removeGoal(mob, goal);
        }
    }

    @Override
    public <T extends Mob> void removeGoal(@NotNull T mob, @NotNull GoalKey<T> key) {
        for (Goal<T> goal : getGoals(mob, key)) {
            removeGoal(mob, goal);
        }
    }

    @Override
    public <T extends Mob> boolean hasGoal(@NotNull T mob, @NotNull GoalKey<T> key) {
        for (Goal<T> g : getAllGoals(mob)) {
            if (g.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T extends Mob> Goal<T> getGoal(@NotNull T mob, @NotNull GoalKey<T> key) {
        for (Goal<T> g : getAllGoals(mob)) {
            if (g.getKey().equals(key)) {
                return g;
            }
        }
        return null;
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getGoals(@NotNull T mob, @NotNull GoalKey<T> key) {
        Set<Goal<T>> goals = new HashSet<>();
        for (Goal<T> g : getAllGoals(mob)) {
            if (g.getKey().equals(key)) {
                goals.add(g);
            }
        }
        return goals;
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getAllGoals(@NotNull T mob) {
        Set<Goal<T>> goals = new HashSet<>();
        for (GoalType type : GoalType.values()) {
            goals.addAll(getAllGoals(mob, type));
        }
        return goals;
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getAllGoals(@NotNull T mob, @NotNull GoalType type) {
        Set<Goal<T>> goals = new HashSet<>();
        for (WrappedGoal item : getHandle(mob, type).getAvailableGoals()) {
            if (MobGoalHelper.hasType(item.getGoal(), type)) {
                goals.add(toApi(item));
            }
        }
        return goals;
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getAllGoalsWithout(@NotNull T mob, @NotNull GoalType type) {
        Set<Goal<T>> goals = new HashSet<>();
        for (GoalType internalType : GoalType.values()) {
            if (internalType == type) {
                continue;
            }
            for (WrappedGoal item : getHandle(mob, internalType).getAvailableGoals()) {
                if (!MobGoalHelper.hasType(item.getGoal(), type)) {
                    goals.add(toApi(item));
                }
            }
        }
        return goals;
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getRunningGoals(@NotNull T mob) {
        Set<Goal<T>> goals = new HashSet<>();
        for (GoalType type : GoalType.values()) {
            goals.addAll(getRunningGoals(mob, type));
        }
        return goals;
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getRunningGoals(@NotNull T mob, @NotNull GoalType type) {
        Set<Goal<T>> goals = new HashSet<>();
        for (WrappedGoal item : getHandle(mob, type).getAvailableGoals()) {
            if (item.isRunning() && MobGoalHelper.hasType(item.getGoal(), type)) {
                goals.add(toApi(item));
            }
        }
        return goals;
    }

    @Override
    public <T extends Mob> @NotNull Collection<Goal<T>> getRunningGoalsWithout(@NotNull T mob, @NotNull GoalType type) {
        Set<Goal<T>> goals = new HashSet<>();
        for (GoalType internalType : GoalType.values()) {
            if (internalType == type) {
                continue;
            }
            for (WrappedGoal item : getHandle(mob, internalType).getAvailableGoals()) {
                if (item.isRunning() && !MobGoalHelper.hasType(item.getGoal(), type)) {
                    goals.add(toApi(item));
                }
            }
        }
        return goals;
    }

    private static <T extends Mob> Goal<T> toApi(WrappedGoal item) {
        if (item.getGoal() instanceof PaperCustomGoal<?> customGoal) {
            //noinspection unchecked
            return (Goal<T>) customGoal.getHandle();
        }
        return PaperVanillaGoal.of(item.getGoal());
    }

    private static GoalSelector getHandle(Mob mob, EnumSet<GoalType> types) {
        return types.contains(GoalType.TARGET) ? FusionCoreMobs.mob(mob).targetSelector : FusionCoreMobs.mob(mob).goalSelector;
    }

    private static GoalSelector getHandle(Mob mob, GoalType type) {
        return type == GoalType.TARGET ? FusionCoreMobs.mob(mob).targetSelector : FusionCoreMobs.mob(mob).goalSelector;
    }
}
