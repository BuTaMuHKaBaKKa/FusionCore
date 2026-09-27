package io.izzel.arclight.common.bridge.bukkit;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;

import java.util.logging.Logger;

public interface PluginClassLoaderBridge {
    PluginDescriptionFile arclight$desc();
    Class<?> arclight$loadFromExternal(String name, boolean initialize, boolean checkLibraries) throws ClassNotFoundException;
    SimplePluginManager arclight$getPluginManager();
    Logger arclight$systemLogger();

    // Fusion Core: a class loader prepared for a Paper plugin's bootstrap before Bukkit's loader exists
    void fusioncore$attach(org.bukkit.plugin.java.JavaPluginLoader loader);

    void fusioncore$setPlugin(org.bukkit.plugin.java.JavaPlugin plugin);

    void fusioncore$setLibraryLoader(ClassLoader libraryLoader);

    /** Fusion Core: the Bukkit loader, null during an early bootstrap. */
    org.bukkit.plugin.java.JavaPluginLoader fusioncore$loader();
}
