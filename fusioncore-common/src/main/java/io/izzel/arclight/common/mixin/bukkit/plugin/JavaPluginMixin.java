package io.izzel.arclight.common.mixin.bukkit.plugin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.log.ArclightPluginLogger;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.PaperLifecycleEventManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(JavaPlugin.class)
public class JavaPluginMixin {

    @Redirect(method = "init", remap = false, at = @At(value = "NEW", target = "(Lorg/bukkit/plugin/Plugin;)Lorg/bukkit/plugin/PluginLogger;"))
    private PluginLogger arclight$createLogger(Plugin plugin) {
        return new ArclightPluginLogger(plugin);
    }

    // Fusion Core: Paper's lifecycle event manager (patch 0441): handlers can be registered only while the plugin enables

    @Unique private LifecycleEventManager<?> fusioncore$lifecycleEventManager;
    @Unique private boolean fusioncore$allowsLifecycleRegistration = true;

    @SuppressWarnings({"rawtypes", "unchecked"}) // LifecycleEventManager<Plugin> in Paper; javac does not see Plugin as a LifecycleEventOwner
    public LifecycleEventManager getLifecycleManager() {
        if (this.fusioncore$lifecycleEventManager == null) {
            this.fusioncore$lifecycleEventManager = new PaperLifecycleEventManager((LifecycleEventOwner) (Object) this, () -> this.fusioncore$allowsLifecycleRegistration);
        }
        return this.fusioncore$lifecycleEventManager;
    }

    @WrapOperation(method = "setEnabled", remap = false, at = @At(value = "INVOKE", remap = false, target = "Lorg/bukkit/plugin/java/JavaPlugin;onEnable()V"))
    private void fusioncore$lockLifecycleRegistration(JavaPlugin plugin, Operation<Void> original) {
        try {
            original.call(plugin);
        } finally {
            this.fusioncore$allowsLifecycleRegistration = false;
        }
    }

    // Fusion Core: Paper's public init (API patch 0009), for plugin managers that construct a plugin themselves. The plugin
    // keeps Bukkit's loader (of its class loader, else the one it had), which enabling and disabling go through; Spigot's logger
    // field holds a PluginLogger, so another logger passed here is not used.

    @org.spongepowered.asm.mixin.Shadow(remap = false) private org.bukkit.plugin.PluginLoader loader;
    @org.spongepowered.asm.mixin.Shadow(remap = false) private org.bukkit.Server server;
    @org.spongepowered.asm.mixin.Shadow(remap = false) private java.io.File file;
    @org.spongepowered.asm.mixin.Shadow(remap = false) private org.bukkit.plugin.PluginDescriptionFile description;
    @org.spongepowered.asm.mixin.Shadow(remap = false) private java.io.File dataFolder;
    @org.spongepowered.asm.mixin.Shadow(remap = false) private ClassLoader classLoader;
    @org.spongepowered.asm.mixin.Shadow(remap = false) private java.io.File configFile;
    @org.spongepowered.asm.mixin.Shadow(remap = false) private PluginLogger logger;

    public final void init(org.bukkit.Server server, org.bukkit.plugin.PluginDescriptionFile description, java.io.File dataFolder, java.io.File file,
                           ClassLoader classLoader, io.papermc.paper.plugin.configuration.PluginMeta configuration, java.util.logging.Logger logger) {
        if (classLoader instanceof io.izzel.arclight.common.bridge.bukkit.PluginClassLoaderBridge bridge && bridge.fusioncore$loader() != null) {
            this.loader = bridge.fusioncore$loader();
        }
        this.server = server;
        this.file = file;
        this.description = description;
        this.dataFolder = dataFolder;
        this.classLoader = classLoader;
        this.configFile = new java.io.File(dataFolder, "config.yml");
        this.logger = logger instanceof PluginLogger pluginLogger ? pluginLogger : new ArclightPluginLogger((Plugin) (Object) this);
    }
}
