package com.destroystokyo.paper.utils;

import io.izzel.arclight.common.mod.util.log.ArclightPluginLogger;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.logging.Logger;

/**
 * Fusion Core: stand-in for Paper's class, so plugins that call {@code PaperPluginLogger.getLogger} load on Spigot.
 * Returns the plugin's regular logger.
 */
public class PaperPluginLogger extends Logger {

    protected PaperPluginLogger(final String name) {
        super(name, null);
    }

    public static Logger getLogger(final PluginDescriptionFile description) {
        final String prefix = description.getPrefix();
        return ArclightPluginLogger.getLogger(prefix != null ? prefix : description.getName());
    }

    /** Paper's overload for any plugin meta (paper-plugin.yml plugins too). */
    public static Logger getLogger(final io.papermc.paper.plugin.configuration.PluginMeta meta) {
        final String prefix = meta.getLoggerPrefix();
        return ArclightPluginLogger.getLogger(prefix != null ? prefix : meta.getName());
    }
}
