package co.aikar.timings;

import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Fusion Core: the public part of Paper's {@code TimingsManager}; all handlers are no-ops.
 *
 * @deprecated Timings will be removed in the future
 */
@Deprecated(forRemoval = true)
@SuppressWarnings("removal")
public final class TimingsManager {

    public static final FullServerTickHandler FULL_SERVER_TICK = new FullServerTickHandler();
    public static final TimingHandler TIMINGS_TICK = new TimingHandler();
    public static final Timing PLUGIN_GROUP_HANDLER = Timings.NULL_HANDLER;
    public static String url = "https://timings.aikar.co/";
    public static List<String> hiddenConfigs = new ArrayList<String>();
    public static boolean privacy = false;

    private TimingsManager() {
    }

    /** Paper: the timing of a command, grouped under its plugin; always the no-op timing here. */
    @NotNull
    public static Timing getCommandTiming(@Nullable String pluginName, @NotNull Command command) {
        return Timings.NULL_HANDLER;
    }

    /**
     * Looks up the class loader for the specified class, and if it is a PluginClassLoader, return the
     * Plugin that created this class.
     *
     * @param clazz Class to check
     * @return Plugin if created by a plugin
     */
    @Nullable
    public static Plugin getPluginByClassloader(@Nullable Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        try {
            return JavaPlugin.getProvidingPlugin(clazz);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // not a plugin class, or its plugin is not initialized yet (Paper returns the plugin field, null then)
            return null;
        }
    }
}
