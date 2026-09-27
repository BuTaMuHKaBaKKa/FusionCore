package org.bukkit.plugin.java;

import java.net.URL;
import java.net.URLClassLoader;

/** Fusion Core: compile-time stub only; {@code PublicClassProcessor} makes the real class public at runtime. */
public final class PluginClassLoader extends URLClassLoader {

    private PluginClassLoader() {
        super(new URL[0]);
    }
}
