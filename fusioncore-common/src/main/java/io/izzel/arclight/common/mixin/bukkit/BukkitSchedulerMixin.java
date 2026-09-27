package io.izzel.arclight.common.mixin.bukkit;

import com.google.common.base.Preconditions;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.Executor;

/** Fusion Core: Paper's {@code BukkitScheduler#getMainThreadExecutor} (patch 0488): each command runs as a next-tick task. */
@Mixin(value = BukkitScheduler.class, remap = false)
public interface BukkitSchedulerMixin {

    default Executor getMainThreadExecutor(Plugin plugin) {
        Preconditions.checkArgument(plugin != null, "Plugin cannot be null");
        return command -> {
            Preconditions.checkArgument(command != null, "Command cannot be null");
            ((BukkitScheduler) this).runTask(plugin, command);
        };
    }
}
