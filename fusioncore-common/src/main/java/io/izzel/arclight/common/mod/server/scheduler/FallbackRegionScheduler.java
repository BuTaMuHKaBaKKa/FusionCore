package io.izzel.arclight.common.mod.server.scheduler;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/** Fusion Core: Paper's {@code FallbackRegionScheduler} (patch 0843); region tasks are global tasks. */
public final class FallbackRegionScheduler implements RegionScheduler {

    @Override
    public void execute(@NotNull final Plugin plugin, @NotNull final World world, final int chunkX, final int chunkZ, @NotNull final Runnable run) {
        PaperSchedulers.GLOBAL.execute(plugin, run);
    }

    @Override
    public @NotNull ScheduledTask run(@NotNull final Plugin plugin, @NotNull final World world, final int chunkX, final int chunkZ, @NotNull final Consumer<ScheduledTask> task) {
        return PaperSchedulers.GLOBAL.run(plugin, task);
    }

    @Override
    public @NotNull ScheduledTask runDelayed(@NotNull final Plugin plugin, @NotNull final World world, final int chunkX, final int chunkZ, @NotNull final Consumer<ScheduledTask> task, final long delayTicks) {
        return PaperSchedulers.GLOBAL.runDelayed(plugin, task, delayTicks);
    }

    @Override
    public @NotNull ScheduledTask runAtFixedRate(@NotNull final Plugin plugin, @NotNull final World world, final int chunkX, final int chunkZ, @NotNull final Consumer<ScheduledTask> task, final long initialDelayTicks, final long periodTicks) {
        return PaperSchedulers.GLOBAL.runAtFixedRate(plugin, task, initialDelayTicks, periodTicks);
    }
}
