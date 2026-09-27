package co.aikar.timings;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Fusion Core: Paper's timed event executor. Timings are off, so it just calls the wrapped executor.
 *
 * @deprecated Timings will be removed in the future
 */
@Deprecated(forRemoval = true)
public class TimedEventExecutor implements EventExecutor {

    private final EventExecutor executor;

    /**
     * Wraps an event executor and associates a timing handler to it.
     *
     * @param executor Executor to wrap
     * @param plugin Owning plugin
     * @param method EventHandler method
     * @param eventClass Owning class
     */
    public TimedEventExecutor(@NotNull EventExecutor executor, @NotNull Plugin plugin, @Nullable Method method, @NotNull Class<? extends Event> eventClass) {
        this.executor = executor;
    }

    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
        executor.execute(listener, event);
    }

    @Override
    @NotNull
    public String toString() {
        return "TimedEventExecutor['" + this.executor.toString() + "']";
    }
}
