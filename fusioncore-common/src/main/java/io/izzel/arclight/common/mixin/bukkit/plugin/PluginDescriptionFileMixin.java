package io.izzel.arclight.common.mixin.bukkit.plugin;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoadOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

/** Fusion Core: {@link PluginDescriptionFile} as Paper's {@link PluginMeta}. */
@Mixin(value = PluginDescriptionFile.class, remap = false)
public abstract class PluginDescriptionFileMixin implements PluginMeta, io.izzel.arclight.common.bridge.bukkit.PluginDescriptionFileBridge {

    @Shadow public abstract String getMain();

    @Shadow public abstract PluginLoadOrder getLoad();

    @Shadow public abstract String getPrefix();

    @Shadow public abstract List<String> getDepend();

    @Shadow public abstract List<String> getSoftDepend();

    @Shadow public abstract List<String> getLoadBefore();

    @Shadow public abstract List<String> getProvides();

    @Override
    public String getMainClass() {
        return this.getMain();
    }

    @Override
    public PluginLoadOrder getLoadOrder() {
        return this.getLoad();
    }

    @Override
    public String getLoggerPrefix() {
        return this.getPrefix();
    }

    @Override
    public List<String> getPluginDependencies() {
        return this.getDepend();
    }

    @Override
    public List<String> getPluginSoftDependencies() {
        return this.getSoftDepend();
    }

    @Override
    public List<String> getLoadBeforePlugins() {
        return this.getLoadBefore();
    }

    @Override
    public List<String> getProvidedPlugins() {
        return this.getProvides();
    }

    // Fusion Core: Paper's plugin loader API for plugin.yml plugins (API patch 0009): "paper-plugin-loader" names a PluginLoader
    // whose libraries join the plugin's (PluginClassLoaderMixin), "paper-skip-libraries: true" drops Spigot's "libraries" list

    @Shadow private String rawName;
    @Shadow private String name;
    @Shadow private List<String> provides;
    @Shadow private String main;
    @Shadow private String classLoaderOf;
    @Shadow private List<String> depend;
    @Shadow private List<String> softDepend;
    @Shadow private List<String> loadBefore;
    @Shadow private String version;
    @Shadow private java.util.Map<String, java.util.Map<String, Object>> commands;
    @Shadow private String description;
    @Shadow private List<String> authors;
    @Shadow private List<String> contributors;
    @Shadow private String website;
    @Shadow private String prefix;
    @Shadow private PluginLoadOrder order;
    @Shadow private List<org.bukkit.permissions.Permission> permissions;
    @Shadow private org.bukkit.permissions.PermissionDefault defaultPerm;
    @Shadow private java.util.Set<org.bukkit.plugin.PluginAwareness> awareness;
    @Shadow private String apiVersion;
    @Shadow private List<String> libraries;

    private String fusioncore$paperLoader;

    public String getPaperPluginLoader() {
        return this.fusioncore$paperLoader;
    }

    @Override
    public String fusioncore$paperPluginLoader() {
        return this.fusioncore$paperLoader;
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "loadMap", at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private void fusioncore$paperKeys(java.util.Map<?, ?> map, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (map.containsKey("paper-plugin-loader")) {
            this.fusioncore$paperLoader = map.get("paper-plugin-loader").toString();
        }
        if (map.containsKey("paper-skip-libraries") && map.get("paper-skip-libraries").toString().equalsIgnoreCase("true")) {
            this.libraries = com.google.common.collect.ImmutableList.of();
        }
    }

    /** Fusion Core: Paper's internal all-fields constructor ("Don't use this"), as Paper has it. */
    @io.izzel.arclight.common.mod.mixins.annotation.CreateConstructor
    public void fusioncore$constructor(String rawName, String name, List<String> provides, String main, String classLoaderOf, List<String> depend,
                                     List<String> softDepend, List<String> loadBefore, String version, java.util.Map<String, java.util.Map<String, Object>> commands,
                                     String description, List<String> authors, List<String> contributors, String website, String prefix,
                                     PluginLoadOrder order, List<org.bukkit.permissions.Permission> permissions, org.bukkit.permissions.PermissionDefault defaultPerm,
                                     java.util.Set<org.bukkit.plugin.PluginAwareness> awareness, String apiVersion, List<String> libraries) {
        this.rawName = rawName;
        this.name = name;
        this.provides = provides;
        this.main = main;
        this.classLoaderOf = classLoaderOf;
        this.depend = depend;
        this.softDepend = softDepend;
        this.loadBefore = loadBefore;
        this.version = version;
        this.commands = commands;
        this.description = description;
        this.authors = authors;
        this.contributors = contributors;
        this.website = website;
        this.prefix = prefix;
        this.order = order;
        this.permissions = permissions;
        this.defaultPerm = defaultPerm;
        this.awareness = awareness;
        this.apiVersion = apiVersion;
        this.libraries = libraries;
    }
}
