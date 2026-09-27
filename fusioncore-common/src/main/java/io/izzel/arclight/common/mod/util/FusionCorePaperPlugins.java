package io.izzel.arclight.common.mod.util;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.PaperLifecycleEventManager;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.ClassPathLibrary;
import io.papermc.paper.plugin.loader.library.LibraryLoadingException;
import io.izzel.arclight.common.mod.util.remapper.generated.RemappingURLClassLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Fusion Core: plugins described by {@code paper-plugin.yml}, loaded by Bukkit's loader. Dependencies map to Bukkit's
 * {@code depend}/{@code softdepend}/{@code loadbefore}.
 * <p>
 * Bootstrappers run before the data registries load, so registry events apply. Unlike Paper they run after mods, in
 * jar name order, and class loader isolation is looser.
 */
public final class FusionCorePaperPlugins {

    public static final String PAPER_PLUGIN_YML = "paper-plugin.yml";

    /** What Bukkit's description does not hold, by the plugin jar's absolute path. */
    private static final Map<String, Info> INFO = new ConcurrentHashMap<>();

    public record Info(String name, @Nullable String loader, @Nullable String bootstrapper) {
    }

    private FusionCorePaperPlugins() {
    }

    public static @Nullable Info info(File file) {
        return INFO.get(file.getAbsolutePath());
    }

    /**
     * The description of a Paper plugin jar, or {@code null} when the jar has no {@code paper-plugin.yml}.
     */
    @SuppressWarnings("unchecked")
    public static @Nullable PluginDescriptionFile describe(File file) throws InvalidDescriptionException {
        Map<String, Object> paper;
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry(PAPER_PLUGIN_YML);
            if (entry == null) {
                return null;
            }
            try (Reader reader = new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8)) {
                Object loaded = new Yaml().load(reader);
                if (!(loaded instanceof Map<?, ?> map)) {
                    throw new InvalidDescriptionException(PAPER_PLUGIN_YML + " is not a map");
                }
                paper = (Map<String, Object>) map;
            }
        } catch (IOException e) {
            throw new InvalidDescriptionException(e);
        }
        for (String required : List.of("name", "main", "version", "api-version")) {
            if (paper.get(required) == null) {
                throw new InvalidDescriptionException(PAPER_PLUGIN_YML + " has no " + required);
            }
        }
        Map<String, Object> bukkit = new LinkedHashMap<>();
        for (String key : List.of("name", "version", "main", "description", "website", "prefix", "api-version", "author", "authors",
            "contributors", "load", "provides", "permissions")) {
            if (paper.get(key) != null) {
                bukkit.put(key, paper.get(key));
            }
        }
        bukkit.put("version", String.valueOf(paper.get("version")));
        bukkit.put("api-version", String.valueOf(paper.get("api-version")));
        Object defaultPerm = paper.getOrDefault("defaultPerm", paper.get("default-perm"));
        if (defaultPerm != null) {
            bukkit.put("default-permission", defaultPerm);
        }
        List<String> depend = new ArrayList<>();
        List<String> softDepend = new ArrayList<>();
        List<String> loadBefore = new ArrayList<>();
        if (paper.get("dependencies") instanceof Map<?, ?> dependencies && dependencies.get("server") instanceof Map<?, ?> server) {
            for (Map.Entry<?, ?> dependency : server.entrySet()) {
                String name = String.valueOf(dependency.getKey());
                Map<?, ?> config = dependency.getValue() instanceof Map<?, ?> m ? m : Map.of();
                boolean required = !(config.get("required") instanceof Boolean b) || b; // Paper: required unless said otherwise
                String load = config.get("load") == null ? "OMIT" : String.valueOf(config.get("load")).toUpperCase(Locale.ROOT);
                if (load.equals("AFTER")) {
                    loadBefore.add(name); // the dependency loads after this plugin
                } else if (required) {
                    depend.add(name);
                } else if (load.equals("BEFORE")) {
                    softDepend.add(name);
                }
            }
        }
        bukkit.put("depend", depend);
        bukkit.put("softdepend", softDepend);
        bukkit.put("loadbefore", loadBefore);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        PluginDescriptionFile description = new PluginDescriptionFile(new StringReader(new Yaml(options).dump(bukkit)));
        INFO.put(file.getAbsolutePath(), new Info(description.getName(), string(paper.get("loader")), string(paper.get("bootstrapper"))));
        return description;
    }

    private static @Nullable String string(@Nullable Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Runs the plugin's {@link PluginLoader} with a class of the plugin's own class loader, as Paper does, and returns the
     * class loader of the libraries it asked for, or {@code null} without a loader or libraries.
     */
    public static @Nullable ClassLoader runLoader(Info info, ClassLoader pluginLoader, PluginDescriptionFile description, File file) throws InvalidPluginException {
        if (info.loader() == null) {
            return null;
        }
        List<URL> urls = loaderLibraries(info.loader(), pluginLoader, description, file);
        if (urls.isEmpty()) {
            return null;
        }
        // as Bukkit's libraries from plugin.yml (LibraryLoaderMixin)
        return new RemappingURLClassLoader(description.getName() + "/L", urls.toArray(new URL[0]), JavaPlugin.class.getClassLoader());
    }

    /** Paper's "paper-plugin-loader" for plugin.yml plugins: its libraries join Spigot's "libraries" in one class loader. */
    public static @Nullable ClassLoader runPluginYmlLoader(String loaderClass, ClassLoader pluginLoader, PluginDescriptionFile description, File file,
                                                           @Nullable ClassLoader spigotLibraries) throws InvalidPluginException {
        List<URL> urls = loaderLibraries(loaderClass, pluginLoader, description, file);
        if (urls.isEmpty()) {
            return spigotLibraries;
        }
        if (spigotLibraries instanceof URLClassLoader spigot) {
            List<URL> all = new ArrayList<>(List.of(spigot.getURLs()));
            all.addAll(urls);
            urls = all;
            try {
                spigot.close();
            } catch (IOException ignored) {
            }
        }
        return new RemappingURLClassLoader(description.getName() + "/L", urls.toArray(new URL[0]), JavaPlugin.class.getClassLoader());
    }

    private static List<URL> loaderLibraries(String loaderClass, ClassLoader pluginLoader, PluginDescriptionFile description, File file) throws InvalidPluginException {
        Context context = new Context(description, file);
        List<Path> libraries = new ArrayList<>();
        List<ClassPathLibrary> requested = new ArrayList<>();
        try {
            PluginLoader loader = instantiate(loaderClass, PluginLoader.class, pluginLoader);
            loader.classloader(new PluginClasspathBuilder() {
                @Override
                public PluginClasspathBuilder addLibrary(ClassPathLibrary classPathLibrary) {
                    requested.add(classPathLibrary);
                    return this;
                }

                @Override
                public io.papermc.paper.plugin.bootstrap.PluginProviderContext getContext() {
                    return context;
                }
            });
            for (ClassPathLibrary library : requested) {
                library.register(libraries::add);
            }
        } catch (LibraryLoadingException e) {
            throw new InvalidPluginException("Could not load the libraries of " + description.getName(), e);
        } catch (Throwable e) {
            throw new InvalidPluginException("Could not run the plugin loader " + loaderClass, e);
        }
        List<URL> urls = new ArrayList<>(libraries.size());
        for (Path library : libraries) {
            try {
                urls.add(library.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new InvalidPluginException(e);
            }
        }
        return urls;
    }

    /**
     * The plugin instance: through its {@link PluginBootstrap} (bootstrap, then {@code createPlugin}), or {@code null} without
     * a bootstrapper (Bukkit then creates the main class). Used for plugins that were not bootstrapped at startup.
     */
    public static @Nullable JavaPlugin bootstrap(Info info, ClassLoader pluginLoader, PluginDescriptionFile description, File file) throws InvalidPluginException {
        if (info.bootstrapper() == null) {
            return null;
        }
        return createPlugin(runBootstrap(info, pluginLoader, description, file), description);
    }

    private record Bootstrapped(PluginBootstrap bootstrap, Context context) {
    }

    private static Bootstrapped runBootstrap(Info info, ClassLoader pluginLoader, PluginDescriptionFile description, File file) throws InvalidPluginException {
        Context context = new Context(description, file);
        PluginBootstrap bootstrap;
        try {
            bootstrap = instantiate(info.bootstrapper(), PluginBootstrap.class, pluginLoader);
        } catch (Throwable e) {
            throw new InvalidPluginException("Could not create the bootstrapper " + info.bootstrapper(), e);
        }
        context.bootstrapping = true;
        try {
            bootstrap.bootstrap(context);
        } catch (Throwable e) {
            throw new InvalidPluginException("Could not bootstrap " + description.getName(), e);
        } finally {
            context.bootstrapping = false;
        }
        return new Bootstrapped(bootstrap, context);
    }

    private static JavaPlugin createPlugin(Bootstrapped bootstrapped, PluginDescriptionFile description) throws InvalidPluginException {
        try {
            return bootstrapped.bootstrap().createPlugin(bootstrapped.context());
        } catch (Throwable e) {
            throw new InvalidPluginException("Could not create " + description.getName() + " with its bootstrapper", e);
        }
    }

    // early bootstrap: Paper's LaunchEntryPointHandler#enterBootstrappers

    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger("Fusion Core");

    /** A plugin bootstrapped at startup, by the jar's absolute path: its class loader, or why it failed. */
    private record Early(PluginDescriptionFile description, @Nullable URLClassLoader loader, @Nullable Bootstrapped bootstrapped,
                         @Nullable Throwable failure) {
    }

    private static final Map<String, Early> EARLY = new ConcurrentHashMap<>();
    private static volatile boolean entered;
    private static volatile java.util.Set<String> earlyCompatibilities = java.util.Set.of();

    /**
     * Runs the loaders and bootstrappers of the Paper plugins in the plugins folder, once, before the data registries load
     * (called from {@code WorldLoaderMixin}), then the registry events of the built-in registries.
     */
    public static synchronized void enterBootstrappers() {
        if (entered) {
            return;
        }
        entered = true;
        File pluginsFolder = new File("plugins");
        File bukkitYml = new File("bukkit.yml");
        try {
            joptsimple.OptionSet options = new BukkitOptionParser().parse(java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toArray(new String[0]));
            pluginsFolder = (File) options.valueOf("plugins");
            bukkitYml = (File) options.valueOf("bukkit-settings");
        } catch (Exception ignored) {
            // as MinecraftServerMixin#arclight$loadOptions: the defaults
        }
        org.bukkit.configuration.file.YamlConfiguration bukkitConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(bukkitYml);
        File updateFolder = new File(pluginsFolder, bukkitConfig.getString("settings.update-folder", "update"));
        org.bukkit.configuration.ConfigurationSection compatibilities = bukkitConfig.getConfigurationSection("settings.compatibility");
        if (compatibilities != null) {
            // CraftServer#loadCompatibilities, for classes loaded before the server exists
            earlyCompatibilities = compatibilities.getKeys(false).stream().filter(compatibilities::getBoolean).collect(java.util.stream.Collectors.toUnmodifiableSet());
        }
        File[] jars = pluginsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars != null) {
            java.util.Arrays.sort(jars);
            Map<String, Integer> names = new java.util.HashMap<>();
            Map<File, PluginDescriptionFile> descriptions = new LinkedHashMap<>();
            for (File jar : jars) {
                if (new File(updateFolder, jar.getName()).isFile()) {
                    continue; // Bukkit replaces the jar first; it is bootstrapped when loaded
                }
                try {
                    PluginDescriptionFile description = describe(jar);
                    Info info = description == null ? null : info(jar);
                    if (info != null && info.bootstrapper() != null) {
                        descriptions.put(jar, description);
                        names.merge(description.getName().toLowerCase(Locale.ROOT), 1, Integer::sum);
                    }
                } catch (Throwable e) {
                    // Bukkit reports a broken description when it loads the plugins
                }
            }
            for (Map.Entry<File, PluginDescriptionFile> entry : descriptions.entrySet()) {
                if (names.get(entry.getValue().getName().toLowerCase(Locale.ROOT)) == 1) { // Bukkit refuses ambiguous names
                    bootstrapEarly(entry.getKey(), entry.getValue());
                }
            }
        }
        io.papermc.paper.registry.PaperRegistryListenerManager.INSTANCE.runBuiltInListeners();
    }

    private static void bootstrapEarly(File file, PluginDescriptionFile description) {
        Info info = info(file);
        URLClassLoader loader = null;
        try {
            Class<?> pluginClassLoader = Class.forName("org.bukkit.plugin.java.PluginClassLoader", true, org.bukkit.plugin.java.JavaPluginLoader.class.getClassLoader());
            var constructor = pluginClassLoader.getDeclaredConstructor(String.class, org.bukkit.plugin.java.JavaPluginLoader.class, ClassLoader.class,
                PluginDescriptionFile.class, File.class, File.class, ClassLoader.class);
            constructor.setAccessible(true);
            // no JavaPluginLoader yet: PluginClassLoaderMixin then only prepares the loader (see #takeEarly)
            loader = (URLClassLoader) constructor.newInstance(description.getName(), null, org.bukkit.plugin.java.JavaPluginLoader.class.getClassLoader(),
                description, new File(file.getParentFile(), description.getName()), file, null);
            ClassLoader libraries = runLoader(info, loader, description, file);
            if (libraries != null) {
                ((io.izzel.arclight.common.bridge.bukkit.PluginClassLoaderBridge) loader).fusioncore$setLibraryLoader(libraries);
            }
            EARLY.put(file.getAbsolutePath(), new Early(description, loader, runBootstrap(info, loader, description, file), null));
        } catch (Throwable e) {
            Throwable cause = e instanceof java.lang.reflect.InvocationTargetException ite ? ite.getCause() : e;
            LOGGER.error("Could not bootstrap {} ({})", description.getName(), file.getName(), cause);
            if (loader != null) {
                try {
                    loader.close();
                } catch (IOException ignored) {
                }
            }
            EARLY.put(file.getAbsolutePath(), new Early(description, null, null, cause));
        }
    }

    /**
     * Bukkit's loader creating the class loader of {@code file}: the one prepared at startup, now attached to {@code javaLoader},
     * with the plugin created by its bootstrapper; {@code null} when the plugin was not bootstrapped at startup.
     */
    public static @Nullable URLClassLoader takeEarly(File file, org.bukkit.plugin.java.JavaPluginLoader javaLoader, PluginDescriptionFile description) throws InvalidPluginException {
        Early early = EARLY.remove(file.getAbsolutePath());
        if (early == null) {
            return null;
        }
        if (early.failure() != null) {
            throw new InvalidPluginException("The bootstrap of " + description.getName() + " failed at startup", early.failure());
        }
        if (!early.description().getName().equals(description.getName()) || !early.description().getMain().equals(description.getMain())) {
            try {
                early.loader().close(); // the jar changed since startup
            } catch (IOException ignored) {
            }
            return null;
        }
        var bridge = (io.izzel.arclight.common.bridge.bukkit.PluginClassLoaderBridge) early.loader();
        bridge.fusioncore$attach(javaLoader);
        JavaPlugin plugin = createPlugin(early.bootstrapped(), description);
        if (plugin.getClass().getClassLoader() != early.loader()) {
            throw new InvalidPluginException("The bootstrapper of " + description.getName() + " created a plugin from another class loader");
        }
        bridge.fusioncore$setPlugin(plugin);
        return early.loader();
    }

    /**
     * {@code UnsafeValues#processClass} (Commodore) for a plugin class; before the server exists (early bootstrap) the same
     * conversion with the compatibilities of bukkit.yml, as {@code CraftServer} will set them.
     */
    public static byte[] processClass(PluginDescriptionFile description, String path, byte[] bytes) {
        if (org.bukkit.Bukkit.getServer() != null) {
            return org.bukkit.Bukkit.getUnsafe().processClass(description, path, bytes);
        }
        try {
            var commodore = org.bukkit.craftbukkit.v.util.CraftMagicNumbers.INSTANCE.getCommodore();
            java.util.Set<String> compatibilities = earlyCompatibilities;
            commodore.updateReroute(compatibilities::contains);
            return commodore.convert(bytes, description.getName(), org.bukkit.craftbukkit.v.util.ApiVersion.getOrCreateVersion(description.getAPIVersion()), compatibilities);
        } catch (Exception e) {
            LOGGER.error("Fatal error trying to convert {}:{}", description.getFullName(), path, e);
            return bytes;
        }
    }

    private static <T> T instantiate(String className, Class<T> type, ClassLoader loader) throws ReflectiveOperationException {
        Class<? extends T> cl = Class.forName(className, true, loader).asSubclass(type);
        var constructor = cl.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /** Paper's {@code PluginProviderContextImpl} and {@code PluginBootstrapContextImpl}. */
    private static final class Context implements BootstrapContext {

        private final PluginDescriptionFile description;
        private final File file;
        private final ComponentLogger logger;
        private final LifecycleEventManager<BootstrapContext> lifecycleManager;
        private boolean bootstrapping;

        private Context(PluginDescriptionFile description, File file) {
            this.description = description;
            this.file = file;
            this.logger = ComponentLogger.logger(description.getPrefix() != null ? description.getPrefix() : description.getName());
            this.lifecycleManager = new PaperLifecycleEventManager<>(this, () -> this.bootstrapping);
        }

        @Override
        public PluginMeta getConfiguration() {
            return (PluginMeta) (Object) this.description; // PluginDescriptionFile is a PluginMeta through a mixin
        }

        @Override
        public PluginMeta getPluginMeta() {
            return this.getConfiguration();
        }

        @Override
        public Path getDataDirectory() {
            return new File(this.file.getParentFile(), this.description.getName()).toPath();
        }

        @Override
        public ComponentLogger getLogger() {
            return this.logger;
        }

        @Override
        public Path getPluginSource() {
            return this.file.toPath();
        }

        @Override
        public LifecycleEventManager<BootstrapContext> getLifecycleManager() {
            return this.lifecycleManager;
        }
    }
}
