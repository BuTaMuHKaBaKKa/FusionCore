package io.izzel.arclight.common.mixin.bukkit.plugin;

import com.google.common.collect.Iterators;
import com.google.common.io.ByteStreams;
import io.izzel.arclight.common.bridge.bukkit.JavaPluginLoaderBridge;
import io.izzel.arclight.common.bridge.bukkit.PluginClassLoaderBridge;
import io.izzel.arclight.common.mod.mixins.annotation.CreateConstructor;
import io.izzel.arclight.common.mod.mixins.annotation.ShadowConstructor;
import io.izzel.arclight.common.mod.util.remapper.ArclightRemapConfig;
import io.izzel.arclight.common.mod.util.remapper.ArclightRemapper;
import io.izzel.arclight.common.mod.util.remapper.ClassLoaderRemapper;
import io.izzel.arclight.common.mod.util.remapper.RemappingClassLoader;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.tools.product.Product2;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

@Mixin(targets = "org.bukkit.plugin.java.PluginClassLoader", remap = false)
public abstract class PluginClassLoaderMixin extends URLClassLoader implements RemappingClassLoader, PluginClassLoaderBridge,
    io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader { // Fusion Core: as in Paper

    // @formatter:off
    @Mutable @Shadow @Final private Map<String, Class<?>> classes;
    @Mutable @Shadow @Final private JavaPluginLoader loader;
    @Mutable @Shadow @Final private PluginDescriptionFile description;
    @Mutable @Shadow @Final private Manifest manifest;
    @Mutable @Shadow @Final private URL url;
    @Mutable @Shadow @Final private File dataFolder;
    @Mutable @Shadow @Final private File file;
    @Mutable @Shadow @Final private JarFile jar;
    @Mutable @Shadow @Final private ClassLoader libraryLoader;
    @Mutable @Shadow @Final private JavaPlugin plugin;
    @Mutable @Shadow @Final private Set<String> seenIllegalAccess;
    // @formatter:on

    @Shadow
    abstract void initialize(@NotNull JavaPlugin javaPlugin);

    private ClassLoaderRemapper remapper;

    // Fusion Core: Paper's class loader group and dependency context of a plugin.yml plugin
    private io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup fusioncore$group;
    @io.izzel.arclight.common.mod.mixins.annotation.TransformAccess(org.objectweb.asm.Opcodes.ACC_PUBLIC)
    private io.papermc.paper.plugin.provider.entrypoint.DependencyContext dependencyContext;

    @ShadowConstructor.Super
    public void arclight$constructor$super(String name, URL[] urls, ClassLoader parent) {
        throw new AbstractMethodError();
    }

    @CreateConstructor
    public void arclight$constructor(String name, @NotNull JavaPluginLoader loader, @Nullable ClassLoader parent, @NotNull PluginDescriptionFile description, @NotNull File dataFolder, @NotNull File file, @Nullable ClassLoader libraryLoader) throws IOException, InvalidPluginException {
        arclight$constructor$super(name, new URL[]{file.toURI().toURL()}, parent);
        // Fusion Core: no loader while a Paper plugin is bootstrapped at startup; Bukkit's loader attaches it later
        this.classes = new ConcurrentHashMap<>();
        this.seenIllegalAccess = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.loader = loader;
        this.description = description;
        this.dataFolder = dataFolder;
        this.file = file;
        this.jar = new JarFile(file);
        this.manifest = this.jar.getManifest();
        this.url = file.toURI().toURL();
        this.libraryLoader = libraryLoader;
        this.dependencyContext = io.izzel.arclight.common.mod.util.FusionCorePluginClassLoaders.Dependencies.INSTANCE;
        this.fusioncore$group = io.izzel.arclight.common.mod.util.FusionCorePluginClassLoaders.INSTANCE
            .registerSpigotGroup((org.bukkit.plugin.java.PluginClassLoader) (Object) this);
        if (loader == null) {
            return; // Fusion Core: see FusionCorePaperPlugins#bootstrapEarly
        }

        // Fusion Core: a Paper plugin's loader adds libraries and its bootstrapper creates the plugin
        io.izzel.arclight.common.mod.util.FusionCorePaperPlugins.Info paperInfo = io.izzel.arclight.common.mod.util.FusionCorePaperPlugins.info(file);
        if (paperInfo != null) {
            ClassLoader paperLibraries = io.izzel.arclight.common.mod.util.FusionCorePaperPlugins.runLoader(paperInfo, this, description, file);
            if (paperLibraries != null) {
                this.libraryLoader = paperLibraries;
            }
            JavaPlugin created = io.izzel.arclight.common.mod.util.FusionCorePaperPlugins.bootstrap(paperInfo, this, description, file);
            if (created != null) {
                if (created.getClass().getClassLoader() != this) {
                    throw new InvalidPluginException("The bootstrapper of " + description.getName() + " created a plugin from another class loader");
                }
                this.plugin = created;
                return;
            }
        } else if (((io.izzel.arclight.common.bridge.bukkit.PluginDescriptionFileBridge) (Object) description).fusioncore$paperPluginLoader() != null) {
            // Fusion Core: "paper-plugin-loader" of a plugin.yml plugin, as in Paper
            this.libraryLoader = io.izzel.arclight.common.mod.util.FusionCorePaperPlugins.runPluginYmlLoader(
                ((io.izzel.arclight.common.bridge.bukkit.PluginDescriptionFileBridge) (Object) description).fusioncore$paperPluginLoader(), this, description, file, this.libraryLoader);
        }

        Class<?> jarClass;
        try {
            jarClass = Class.forName(description.getMain(), true, this);
        } catch (ClassNotFoundException ex) {
            throw new InvalidPluginException("Cannot find main class `" + description.getMain() + "'", ex);
        }

        Class<? extends JavaPlugin> pluginClass;
        try {
            pluginClass = jarClass.asSubclass(JavaPlugin.class);
        } catch (ClassCastException ex) {
            throw new InvalidPluginException("main class `" + description.getMain() + "' must extend JavaPlugin", ex);
        }

        Constructor<? extends JavaPlugin> pluginConstructor;
        try {
            pluginConstructor = pluginClass.getDeclaredConstructor();
        } catch (NoSuchMethodException ex) {
            throw new InvalidPluginException("main class `" + description.getMain() + "' must have a public no-args constructor", ex);
        }

        try {
            this.plugin = pluginConstructor.newInstance();
        } catch (IllegalAccessException ex) {
            throw new InvalidPluginException("main class `" + description.getMain() + "' constructor must be public", ex);
        } catch (InstantiationException ex) {
            throw new InvalidPluginException("main class `" + description.getMain() + "' must not be abstract", ex);
        } catch (IllegalArgumentException ex) {
            throw new InvalidPluginException("Could not invoke main class `" + description.getMain() + "' constructor", ex);
        } catch (InvocationTargetException | ExceptionInInitializerError ex) {
            throw new InvalidPluginException("Exception initializing main class `" + description.getMain() + "'", ex);
        }
    }

    @Override
    public ClassLoaderRemapper getRemapper() {
        if (remapper == null) {
            // Fusion Core: a plugin built against Mojang's names is not run through Spigot's mappings, as in Paper
            remapper = io.izzel.arclight.common.mod.util.remapper.MojangClassLoaderRemapper.isMojangMapped(this.manifest, this.jar)
                ? new io.izzel.arclight.common.mod.util.remapper.MojangClassLoaderRemapper(this)
                : ArclightRemapper.createClassLoaderRemapper(this);
        }
        return remapper;
    }

    @Override
    public ArclightRemapConfig getRemapConfig() {
        return ArclightRemapConfig.PLUGIN;
    }

    @Override
    public void fusioncore$attach(JavaPluginLoader loader) {
        this.loader = loader;
        for (Map.Entry<String, Class<?>> entry : this.classes.entrySet()) {
            ((JavaPluginLoaderBridge) (Object) loader).bridge$setClass(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public JavaPluginLoader fusioncore$loader() {
        return this.loader;
    }

    @Override
    public void fusioncore$setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void fusioncore$setLibraryLoader(ClassLoader libraryLoader) {
        this.libraryLoader = libraryLoader;
    }

    @Override
    public PluginDescriptionFile arclight$desc() {
        return description;
    }

    @Override
    public Class<?> arclight$loadFromExternal(String name, boolean initialize, boolean checkLibraries) throws ClassNotFoundException {
        return loadClass0(name, initialize, false, checkLibraries);
    }

    @Override
    public SimplePluginManager arclight$getPluginManager() {
        return (SimplePluginManager) ((JavaPluginLoaderBridge) (Object) loader).arclight$server().getPluginManager();
    }

    @Override
    public Logger arclight$systemLogger() {
        return ((JavaPluginLoaderBridge)(Object) loader).arclight$server().getLogger();
    }

    public PluginClassLoaderMixin(URL[] urls) {
        super(urls);
    }

    /**
     * @author InitAuther97
     * @reason add skip super for Adventure
     */
    @Overwrite
    Class<?> loadClass0(String name, boolean resolve, boolean checkGlobal, boolean checkLibraries) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c != null) {
                if (resolve) resolveClass(c);
                return c;
            }
            if (!ArclightConfig.spec().getCompat().isAdventureIsolatedFromML() || !name.startsWith("net.kyori.adventure.")) {
                try {
                    c = getParent().loadClass(name);
                    if (resolve) resolveClass(c);
                    return c;
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                }
            }

            if (c == null) {
                // If still not found, then invoke findClass in order
                // to find the class.
                try {
                    c = findClass(name);
                    if (resolve) resolveClass(c);
                    return c;
                } catch (ClassNotFoundException e) {
                    // There's no such class
                }
            }
        }

        if (checkLibraries && this.libraryLoader != null) {
            try {
                return this.libraryLoader.loadClass(name);
            } catch (ClassNotFoundException var7) {
            }
        }

        if (checkGlobal && loader != null) { // Fusion Core: no other plugins during an early bootstrap
            // Fusion Core: the plugin's (transitive) dependencies first, as Paper's dependency groups do, then every plugin: a class
            // that another plugin also bundles unrelocated (Multiverse-Inventories and ViaVersion's NBT) comes from the dependency
            Class<?> fromDependency = this.fusioncore$fromDependencies(name, resolve);
            if (fromDependency != null) {
                return fromDependency;
            }
            Class<?> result = ((JavaPluginLoaderBridge)(Object) loader).arclight$getClassByName(name, resolve, this.description);
            if (result != null) {
                // Fusion Core: no "Loaded class … which is not a depend or softdepend" warning, as in Paper ("Totally delete the illegal
                // access logic, we are never going to enforce it anyways here"): nothing is enforced, it only filled the log
                return result;
            }
        }
        if (checkGlobal) { // Fusion Core: class loaders other plugins put into Paper's global group
            Class<?> result = io.izzel.arclight.common.mod.util.FusionCorePluginClassLoaders.INSTANCE.findOutsideBukkit(name, resolve);
            if (result != null) {
                return result;
            }
        }

        throw new ClassNotFoundException(String.format("Plugin %s cannot load class %s", description.getName(), name));
    }

    // dependency loaders, recomputed when the number of loaded plugins changes (a plugin loaded, unloaded or reloaded)
    private transient java.util.List<PluginClassLoaderBridge> fusioncore$dependencyLoaders;
    private transient int fusioncore$dependencyLoadersFor = -1;

    private Class<?> fusioncore$fromDependencies(String name, boolean resolve) {
        if (this.description.getDepend().isEmpty() && this.description.getSoftDepend().isEmpty()) {
            return null;
        }
        var loaders = ((JavaPluginLoaderBridge) (Object) this.loader).arclight$getLoaders();
        java.util.List<PluginClassLoaderBridge> dependencies = this.fusioncore$dependencyLoaders;
        if (dependencies == null || this.fusioncore$dependencyLoadersFor != loaders.size()) {
            SimplePluginManager manager = this.arclight$getPluginManager();
            dependencies = new java.util.ArrayList<>();
            for (var other : loaders) {
                if ((Object) other != this && manager.isTransitiveDepend(this.description, other.arclight$desc())) {
                    dependencies.add(other);
                }
            }
            this.fusioncore$dependencyLoaders = dependencies;
            this.fusioncore$dependencyLoadersFor = loaders.size();
        }
        for (PluginClassLoaderBridge other : dependencies) {
            try {
                return other.arclight$loadFromExternal(name, resolve, true);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    @Override
    public URL getResource(String name) {
        Objects.requireNonNull(name);
        URL url = findResource(name);
        if (url == null) {
            if (getParent() != null) {
                url = getParent().getResource(name);
            }
        }
        return url;
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Objects.requireNonNull(name);
        @SuppressWarnings("unchecked")
        Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
        if (getParent() != null) {
            tmp[1] = getParent().getResources(name);
        }
        tmp[0] = findResources(name);
        return Iterators.asEnumeration(Iterators.concat(Iterators.forEnumeration(tmp[0]), Iterators.forEnumeration(tmp[1])));
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.startsWith("org.bukkit.") || name.startsWith("net.minecraft.")) {
            throw new ClassNotFoundException(name);
        }
        Class<?> result = classes.get(name);

        if (result == null) {
            String path = name.replace('.', '/').concat(".class");
            URL url = this.findResource(path);

            if (url != null) {

                URLConnection connection;
                Callable<byte[]> byteSource;
                try {
                    connection = url.openConnection();
                    connection.connect();
                    byteSource = () -> {
                        try (InputStream is = connection.getInputStream()) {
                            byte[] classBytes = ByteStreams.toByteArray(is);
                            classBytes = ArclightRemapper.SWITCH_TABLE_FIXER.apply(classBytes);
                            classBytes = io.izzel.arclight.common.mod.util.FusionCorePaperPlugins.processClass(description, path, classBytes); // Fusion Core: also before the server exists
                            return classBytes;
                        }
                    };
                } catch (IOException e) {
                    throw new ClassNotFoundException(name, e);
                }

                Product2<byte[], CodeSource> classBytes = this.getRemapper().remapClass(name, byteSource, connection, ArclightRemapConfig.PLUGIN);

                int dot = name.lastIndexOf('.');
                if (dot != -1) {
                    String pkgName = name.substring(0, dot);
                    if (getPackage(pkgName) == null) {
                        try {
                            if (manifest != null) {
                                definePackage(pkgName, manifest, this.url);
                            } else {
                                definePackage(pkgName, null, null, null, null, null, null, null);
                            }
                        } catch (IllegalArgumentException ex) {
                            if (getPackage(pkgName) == null) {
                                throw new IllegalStateException("Cannot find package " + pkgName);
                            }
                        }
                    }
                }

                result = defineClass(name, classBytes._1, 0, classBytes._1.length, classBytes._2);
            }

            if (result == null) {
                result = super.findClass(name);
            }

            if (loader != null) { // Fusion Core: fusioncore$attach publishes classes loaded before
                ((JavaPluginLoaderBridge) (Object) loader).bridge$setClass(name, result);
            }
            classes.put(name, result);
        }

        return result;
    }

    /** Fusion Core: Paper's {@code PluginClassLoader#getPlugin}: the plugin this loader loaded, null until it is constructed. */
    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    // Fusion Core: Paper's ConfiguredPluginClassLoader (API patch 0009)

    @Override
    public io.papermc.paper.plugin.configuration.PluginMeta getConfiguration() {
        return (io.papermc.paper.plugin.configuration.PluginMeta) (Object) this.description;
    }

    @Override
    public Class<?> loadClass(@NotNull String name, boolean resolve, boolean checkGlobal, boolean checkLibraries) throws ClassNotFoundException {
        return this.loadClass0(name, resolve, checkGlobal, checkLibraries);
    }

    @Override
    public void init(JavaPlugin plugin) {
        this.initialize(plugin);
    }

    @Override
    public io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup getGroup() {
        return this.fusioncore$group;
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void fusioncore$unregister(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCorePluginClassLoaders.INSTANCE.unregisterClassloader(this);
    }
}
