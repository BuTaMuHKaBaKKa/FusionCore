package io.izzel.arclight.common.mod.util;

import com.destroystokyo.paper.event.executor.MethodHandleEventExecutor;
import com.destroystokyo.paper.event.executor.StaticMethodHandleEventExecutor;
import com.destroystokyo.paper.event.executor.asm.ASMEventExecutorGenerator;
import com.destroystokyo.paper.event.executor.asm.ClassDefiner;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/** Fusion Core: Paper's statics on {@link EventExecutor}, used only by plugins calling {@code EventExecutor.create}. */
public final class FusionCoreEventExecutorStatics {

    /** Paper's {@code EventExecutor.eventExecutorMap}: generated executor class per handler method. */
    static final ConcurrentMap<Method, Class<? extends EventExecutor>> EVENT_EXECUTOR_MAP = new ConcurrentHashMap<Method, Class<? extends EventExecutor>>() {
        @NotNull
        @Override
        public Class<? extends EventExecutor> computeIfAbsent(@NotNull Method key, @NotNull Function<? super Method, ? extends Class<? extends EventExecutor>> mappingFunction) {
            Class<? extends EventExecutor> executorClass = get(key);
            if (executorClass != null)
                return executorClass;

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (key) {
                executorClass = get(key);
                if (executorClass != null)
                    return executorClass;

                return super.computeIfAbsent(key, mappingFunction);
            }
        }
    };

    private FusionCoreEventExecutorStatics() {
    }

    /** Paper's {@code EventExecutor.create}, as in Paper; the plugin in the non-void warning is named by its description. */
    @NotNull
    @SuppressWarnings("deprecation")
    public static EventExecutor create(@NotNull Method m, @NotNull Class<? extends Event> eventClass) {
        Preconditions.checkNotNull(m, "Null method");
        Preconditions.checkArgument(m.getParameterCount() != 0, "Incorrect number of arguments %s", m.getParameterCount());
        Preconditions.checkArgument(m.getParameterTypes()[0] == eventClass, "First parameter %s doesn't match event class %s", m.getParameterTypes()[0], eventClass);
        ClassDefiner definer = ClassDefiner.getInstance();
        if (m.getReturnType() != Void.TYPE) {
            final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(m.getDeclaringClass());
            org.bukkit.Bukkit.getLogger().warning("@EventHandler method " + m.getDeclaringClass().getName() + (Modifier.isStatic(m.getModifiers()) ? '.' : '#') + m.getName()
                + " returns non-void type " + m.getReturnType().getName() + ". This is unsupported behavior and will no longer work in a future version of Paper."
                + " This should be reported to the developers of " + plugin.getDescription().getFullName() + " (" + String.join(",", plugin.getDescription().getAuthors()) + ')');
        }
        if (Modifier.isStatic(m.getModifiers())) {
            return new StaticMethodHandleEventExecutor(eventClass, m);
        } else if (definer.isBypassAccessChecks() || Modifier.isPublic(m.getDeclaringClass().getModifiers()) && Modifier.isPublic(m.getModifiers())) {
            // get the existing generated EventExecutor class for the Method or generate one
            Class<? extends EventExecutor> executorClass = EVENT_EXECUTOR_MAP.computeIfAbsent(m, (__) -> {
                String name = ASMEventExecutorGenerator.generateName();
                byte[] classData = ASMEventExecutorGenerator.generateEventExecutor(m, name);
                return definer.defineClass(m.getDeclaringClass().getClassLoader(), name, classData).asSubclass(EventExecutor.class);
            });

            try {
                EventExecutor asmExecutor = executorClass.newInstance();
                // Define a wrapper to conform to bukkit stupidity (passing in events that don't match and wrapper exception)
                return new EventExecutor() {
                    @Override
                    public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
                        if (!eventClass.isInstance(event)) return;
                        asmExecutor.execute(listener, event);
                    }

                    @Override
                    @NotNull
                    public String toString() {
                        return "ASMEventExecutor['" + m + "']";
                    }
                };
            } catch (InstantiationException | IllegalAccessException e) {
                throw new AssertionError("Unable to initialize generated event executor", e);
            }
        } else {
            return new MethodHandleEventExecutor(eventClass, m);
        }
    }
}
