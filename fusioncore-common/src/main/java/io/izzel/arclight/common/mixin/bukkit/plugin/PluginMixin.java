package io.izzel.arclight.common.mixin.bukkit.plugin;

import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Plugin#getSLF4JLogger()}, named after the plugin.yml prefix like the plugin's own logger. */
@Mixin(value = Plugin.class, remap = false)
public interface PluginMixin extends io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner {

    default Logger getSLF4JLogger() {
        var description = ((Plugin) this).getDescription();
        String prefix = description.getPrefix();
        return LoggerFactory.getLogger(prefix == null ? description.getName() : prefix);
    }

    /** Fusion Core: Paper's component logger, rendered as plain text. */
    default ComponentLogger getComponentLogger() {
        var description = ((Plugin) this).getDescription();
        String prefix = description.getPrefix();
        return ComponentLogger.logger(prefix == null ? description.getName() : prefix);
    }

    /** Fusion Core: Paper's {@code getPluginMeta()}; for a plugin.yml plugin Paper returns its description file. */
    default PluginMeta getPluginMeta() {
        return (PluginMeta) (Object) ((Plugin) this).getDescription();
    }

    /**
     * Fusion Core: Paper's Log4J logger (patch 0067), under the same name as the SLF4J logger above (Paper uses
     * {@code getLogger().getName()} for both).
     */
    default org.apache.logging.log4j.Logger getLog4JLogger() {
        var description = ((Plugin) this).getDescription();
        String prefix = description.getPrefix();
        return org.apache.logging.log4j.LogManager.getLogger(prefix == null ? description.getName() : prefix);
    }

    /** Fusion Core: Paper's {@code getDataPath()} (patch 0477): the data folder as a path. */
    default java.nio.file.Path getDataPath() {
        return ((Plugin) this).getDataFolder().toPath();
    }

    /**
     * Fusion Core: Paper's lifecycle event manager (patch 0441). Paper declares it abstract; {@code JavaPlugin} has the real one
     * ({@code JavaPluginMixin}), a plugin that is not a {@code JavaPlugin} has none.
     */
    @SuppressWarnings("rawtypes") // LifecycleEventManager<Plugin> in Paper; javac does not see Plugin as a LifecycleEventOwner
    default io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager getLifecycleManager() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
