package io.izzel.arclight.common.mod.util;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.classloader.ClassLoaderAccess;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import io.papermc.paper.plugin.provider.classloader.PaperClassLoaderStorage;
import io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup;
import io.papermc.paper.plugin.provider.entrypoint.DependencyContext;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/** Fusion Core: Paper's plugin class loader storage and {@link DependencyContext} (patch 0019). */
public final class FusionCorePluginClassLoaders implements PaperClassLoaderStorage {

    public static final FusionCorePluginClassLoaders INSTANCE = new FusionCorePluginClassLoaders();

    private final ListGroup global = new ListGroup(new CopyOnWriteArrayList<>(), v -> true);
    private final List<PluginClassLoaderGroup> groups = new CopyOnWriteArrayList<>();

    private FusionCorePluginClassLoaders() {
        this.groups.add(this.global);
    }

    @Override
    public PluginClassLoaderGroup registerSpigotGroup(PluginClassLoader pluginClassLoader) {
        ConfiguredPluginClassLoader self = (ConfiguredPluginClassLoader) (Object) pluginClassLoader;
        return this.registerGroup(self, new SpigotGroup(this.global.loaders,
            library -> Dependencies.INSTANCE.isTransitiveDependency(self.getConfiguration(), library.getConfiguration())));
    }

    @Override
    public PluginClassLoaderGroup registerOpenGroup(ConfiguredPluginClassLoader classLoader) {
        return this.registerGroup(classLoader, this.global);
    }

    @Override
    public PluginClassLoaderGroup registerAccessBackedGroup(ConfiguredPluginClassLoader classLoader, ClassLoaderAccess access) {
        List<ConfiguredPluginClassLoader> allowed = new CopyOnWriteArrayList<>();
        for (ConfiguredPluginClassLoader loader : this.global.loaders) {
            if (access.canAccess(loader)) {
                allowed.add(loader);
            }
        }
        return this.registerGroup(classLoader, new ListGroup(allowed, access));
    }

    private PluginClassLoaderGroup registerGroup(ConfiguredPluginClassLoader classLoader, PluginClassLoaderGroup group) {
        for (PluginClassLoaderGroup existing : this.groups) {
            if (existing.getAccess().canAccess(classLoader)) {
                existing.add(classLoader);
            }
        }
        this.groups.add(group);
        return group;
    }

    @Override
    public void unregisterClassloader(ConfiguredPluginClassLoader configuredPluginClassLoader) {
        this.global.remove(configuredPluginClassLoader);
        PluginClassLoaderGroup own = configuredPluginClassLoader.getGroup();
        if (own != this.global) {
            this.groups.remove(own);
        }
        for (PluginClassLoaderGroup group : this.groups) {
            group.remove(configuredPluginClassLoader);
        }
    }

    @Override
    public boolean registerUnsafePlugin(ConfiguredPluginClassLoader pluginLoader) {
        if (this.global.loaders.contains(pluginLoader)) {
            return false;
        }
        this.global.add(pluginLoader);
        return true;
    }

    /** A class from a global-group loader that is not a Bukkit plugin's own; {@code JavaPluginLoader} has already missed it. */
    public @Nullable Class<?> findOutsideBukkit(String name, boolean resolve) {
        for (ConfiguredPluginClassLoader loader : this.global.loaders) {
            if ((Object) loader instanceof PluginClassLoader) {
                continue;
            }
            try {
                return loader.loadClass(name, resolve, false, true);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    /** Paper's {@code SimpleListPluginClassLoaderGroup}: the requester first, then every member in order. */
    static class ListGroup implements PluginClassLoaderGroup {

        final List<ConfiguredPluginClassLoader> loaders;
        private final ClassLoaderAccess access;

        ListGroup(List<ConfiguredPluginClassLoader> loaders, ClassLoaderAccess access) {
            this.loaders = loaders;
            this.access = access;
        }

        @Override
        public @Nullable Class<?> getClassByName(String name, boolean resolve, ConfiguredPluginClassLoader requester) {
            try {
                return this.lookup(name, false, requester);
            } catch (ClassNotFoundException ignored) {
            }
            for (ConfiguredPluginClassLoader loader : this.loaders) {
                try {
                    return this.lookup(name, resolve, loader);
                } catch (ClassNotFoundException ignored) {
                }
            }
            return null;
        }

        Class<?> lookup(String name, boolean resolve, ConfiguredPluginClassLoader loader) throws ClassNotFoundException {
            return loader.loadClass(name, resolve, false, true);
        }

        @Override
        public void remove(ConfiguredPluginClassLoader configuredPluginClassLoader) {
            this.loaders.remove(configuredPluginClassLoader);
        }

        @Override
        public void add(ConfiguredPluginClassLoader configuredPluginClassLoader) {
            this.loaders.add(configuredPluginClassLoader);
        }

        @Override
        public ClassLoaderAccess getAccess() {
            return this.access;
        }
    }

    /** Paper's {@code SpigotPluginClassLoaderGroup}: a view of the global group; libraries only of dependencies; nobody joins it. */
    static final class SpigotGroup extends ListGroup {

        private final Predicate<ConfiguredPluginClassLoader> libraries;

        SpigotGroup(List<ConfiguredPluginClassLoader> global, Predicate<ConfiguredPluginClassLoader> libraries) {
            super(global, v -> false);
            this.libraries = libraries;
        }

        @Override
        Class<?> lookup(String name, boolean resolve, ConfiguredPluginClassLoader loader) throws ClassNotFoundException {
            return loader.loadClass(name, resolve, false, this.libraries.test(loader));
        }

        @Override
        public void remove(ConfiguredPluginClassLoader configuredPluginClassLoader) {
        }

        @Override
        public void add(ConfiguredPluginClassLoader configuredPluginClassLoader) {
        }
    }

    /** Paper's runtime dependency tree, built from the loaded plugins on each call. */
    public static final class Dependencies implements DependencyContext {

        public static final Dependencies INSTANCE = new Dependencies();

        private Dependencies() {
        }

        @Override
        public boolean isTransitiveDependency(PluginMeta plugin, PluginMeta depend) {
            // Spigot's own graph also knows plugins that are being loaded right now
            if (Bukkit.getServer() != null && Bukkit.getPluginManager() instanceof org.bukkit.plugin.SimplePluginManager manager
                && (Object) plugin instanceof org.bukkit.plugin.PluginDescriptionFile p && (Object) depend instanceof org.bukkit.plugin.PluginDescriptionFile d
                && manager.isTransitiveDepend(p, d)) {
                return true;
            }
            Set<String> targets = new HashSet<>(depend.getProvidedPlugins());
            targets.add(depend.getName());
            Set<String> seen = new HashSet<>();
            Deque<String> queue = new ArrayDeque<>(edges(plugin));
            while (!queue.isEmpty()) {
                String next = queue.poll();
                if (!seen.add(next)) {
                    continue;
                }
                if (targets.contains(next)) {
                    return true;
                }
                PluginMeta meta = next.equals(plugin.getName()) ? plugin : meta(next);
                if (meta != null) {
                    queue.addAll(edges(meta));
                }
            }
            return false;
        }

        private static List<String> edges(PluginMeta meta) {
            List<String> out = new ArrayList<>(meta.getPluginDependencies());
            out.addAll(meta.getPluginSoftDependencies());
            out.addAll(meta.getProvidedPlugins());
            return out;
        }

        private static @Nullable PluginMeta meta(String name) {
            if (Bukkit.getServer() == null) {
                return null;
            }
            Plugin found = Bukkit.getPluginManager().getPlugin(name);
            return found == null || !found.getName().equals(name) ? null : (PluginMeta) (Object) found.getDescription();
        }

        @Override
        public boolean hasDependency(String pluginIdentifier) {
            return Bukkit.getServer() != null && Bukkit.getPluginManager().getPlugin(pluginIdentifier) != null;
        }
    }
}
