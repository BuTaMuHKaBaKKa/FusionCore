package io.papermc.paper.plugin.provider.classloader;

/** Fusion Core: Paper looks the storage up through {@code Services}; Fusion Core has one implementation. */
class PaperClassLoaderStorageAccess {

    static final PaperClassLoaderStorage INSTANCE = io.izzel.arclight.common.mod.util.FusionCorePluginClassLoaders.INSTANCE;
}
