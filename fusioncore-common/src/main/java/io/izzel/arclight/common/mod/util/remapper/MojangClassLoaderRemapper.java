package io.izzel.arclight.common.mod.util.remapper;

import net.md_5.specialsource.JarMapping;
import org.bukkit.craftbukkit.v.CraftServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Fusion Core: remapper for plugins built against Mojang names; only CraftBukkit references move to the versioned package.
 */
public class MojangClassLoaderRemapper extends ClassLoaderRemapper {

    public static final String NAMESPACE_ATTRIBUTE = "paperweight-mappings-namespace";
    private static final String UNVERSIONED = "org/bukkit/craftbukkit/";
    private static final String VERSIONED = CraftServer.class.getName().replace('.', '/').substring(0, CraftServer.class.getName().lastIndexOf('.') + 1);
    private static final Map<String, String> RELOCATED = new ConcurrentHashMap<>();

    public MojangClassLoaderRemapper(ClassLoader classLoader) {
        super(new JarMapping(), new JarMapping(), classLoader);
    }

    /**
     * Whether a plugin jar is built against Mojang's names, by Paper's rules. An unknown namespace is treated as Spigot's
     * (Paper refuses such a plugin).
     */
    public static boolean isMojangMapped(Manifest manifest, JarFile jar) {
        String namespace = manifest == null ? null : manifest.getMainAttributes().getValue(NAMESPACE_ATTRIBUTE);
        if (namespace == null) {
            return jar.getEntry("paper-plugin.yml") != null;
        }
        return namespace.equals("mojang") || namespace.equals("mojang+yarn");
    }

    @Override
    public String map(String typeName) {
        if (typeName.startsWith(UNVERSIONED) && !typeName.startsWith(VERSIONED)) {
            return RELOCATED.computeIfAbsent(typeName, MojangClassLoaderRemapper::relocate);
        }
        return typeName;
    }

    private static String relocate(String typeName) {
        String versioned = VERSIONED + typeName.substring(UNVERSIONED.length());
        ClassLoader core = CraftServer.class.getClassLoader();
        if (core.getResource(typeName + ".class") == null && core.getResource(versioned + ".class") != null) {
            return versioned;
        }
        return typeName;
    }
}
