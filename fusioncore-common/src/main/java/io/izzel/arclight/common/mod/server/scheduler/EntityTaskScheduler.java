package io.izzel.arclight.common.mod.server.scheduler;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v.entity.CraftEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** Fusion Core: Paper's entity scheduler (patch 0843). Created on first use and ticked only while it has tasks. */
public final class EntityTaskScheduler {

    /**
     * The Entity. Note that it is the CraftEntity, since only that class properly tracks world transfers.
     */
    public final CraftEntity entity;

    private record ScheduledTask(Consumer<? extends Entity> run, Consumer<? extends Entity> retired) {}

    private long tickCount = 0L;
    private static final long RETIRED_TICK_COUNT = -1L;
    private final Object stateLock = new Object();
    private final Long2ObjectOpenHashMap<List<ScheduledTask>> oneTimeDelayed = new Long2ObjectOpenHashMap<>();

    private final ArrayDeque<ScheduledTask> currentlyExecuting = new ArrayDeque<>();

    // Fusion Core: whether PaperSchedulers ticks this scheduler; guarded by stateLock
    private boolean active;

    EntityTaskScheduler(final CraftEntity entity, final boolean retired) {
        this.entity = Objects.requireNonNull(entity);
        if (retired) {
            this.tickCount = RETIRED_TICK_COUNT;
        }
    }

    /**
     * Retires the scheduler, preventing new tasks from being scheduled and invoking the retired callback
     * on all currently scheduled tasks. A second call does nothing.
     */
    public void retire() {
        synchronized (this.stateLock) {
            if (this.tickCount == RETIRED_TICK_COUNT) {
                return;
            }
            this.tickCount = RETIRED_TICK_COUNT;
            this.setActive(false);
        }

        final Entity thisEntity = this.entity.getHandle();

        // correctly handle and order retiring while running executeTick
        for (int i = 0, len = this.currentlyExecuting.size(); i < len; ++i) {
            final ScheduledTask task = this.currentlyExecuting.pollFirst();
            @SuppressWarnings("unchecked") final Consumer<Entity> retireTask = (Consumer<Entity>) task.retired;
            if (retireTask == null) {
                continue;
            }

            retireTask.accept(thisEntity);
        }

        for (final List<ScheduledTask> tasks : this.oneTimeDelayed.values()) {
            for (int i = 0, len = tasks.size(); i < len; ++i) {
                final ScheduledTask task = tasks.get(i);
                @SuppressWarnings("unchecked") final Consumer<Entity> retireTask = (Consumer<Entity>) task.retired;
                if (retireTask == null) {
                    continue;
                }

                retireTask.accept(thisEntity);
            }
        }
        this.oneTimeDelayed.clear();
    }

    /**
     * Schedules a task with the given delay. If the task failed to schedule because the scheduler is retired (entity
     * removed), then returns {@code false}. Otherwise, either the run callback will be invoked after the specified delay,
     * or the retired callback will be invoked if the scheduler is retired.
     */
    public boolean schedule(final Consumer<? extends Entity> run, final Consumer<? extends Entity> retired, final long delay) {
        Objects.requireNonNull(run, "Run task may not be null");

        final ScheduledTask task = new ScheduledTask(run, retired);
        synchronized (this.stateLock) {
            if (this.tickCount == RETIRED_TICK_COUNT) {
                return false;
            }
            this.oneTimeDelayed.computeIfAbsent(this.tickCount + Math.max(1L, delay), (final long keyInMap) -> new ArrayList<>()).add(task);
            this.setActive(true);
        }

        return true;
    }

    /**
     * Executes a tick for the scheduler. Main thread only; the entity must be in a world.
     */
    void executeTick() {
        final Entity thisEntity = this.entity.getHandle();

        final List<ScheduledTask> toRun;
        synchronized (this.stateLock) {
            if (this.tickCount == RETIRED_TICK_COUNT) {
                this.setActive(false);
                return;
            }
            ++this.tickCount;
            if (this.oneTimeDelayed.isEmpty()) {
                toRun = null;
            } else {
                toRun = this.oneTimeDelayed.remove(this.tickCount);
            }
        }

        if (toRun != null) {
            for (int i = 0, len = toRun.size(); i < len; ++i) {
                this.currentlyExecuting.addLast(toRun.get(i));
            }
        }

        // Note: It is allowed for the tasks executed to retire the entity in a given task.
        for (int i = 0, len = this.currentlyExecuting.size(); i < len; ++i) {
            final ScheduledTask task = this.currentlyExecuting.pollFirst();

            if (this.tickCount != RETIRED_TICK_COUNT) {
                @SuppressWarnings("unchecked") final Consumer<Entity> run = (Consumer<Entity>) task.run;
                run.accept(thisEntity);
            } else {
                // retired synchronously
                break;
            }
        }

        synchronized (this.stateLock) {
            if (this.oneTimeDelayed.isEmpty() && this.currentlyExecuting.isEmpty()) {
                this.setActive(false);
            }
        }
    }

    private void setActive(final boolean active) {
        if (this.active != active) {
            this.active = active;
            PaperSchedulers.setActive(this, active);
        }
    }
}
