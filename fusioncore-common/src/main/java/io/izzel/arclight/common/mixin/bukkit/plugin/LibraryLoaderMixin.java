package io.izzel.arclight.common.mixin.bukkit.plugin;

import io.izzel.arclight.common.mod.mixins.annotation.TransformAccess;
import io.izzel.arclight.common.mod.util.remapper.generated.RemappingURLClassLoader;
import org.bukkit.plugin.PluginDescriptionFile;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Mixin(targets = "org.bukkit.plugin.java.LibraryLoader", remap = false)
public abstract class LibraryLoaderMixin {

    // Fusion Core: Paper's hooks (API patch 0009), empty as on Paper until someone sets them: the class loader factory for
    // libraries and a remapper of the library jars, both applied to every plugin's libraries
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static BiFunction<URL[], ClassLoader, URLClassLoader> LIBRARY_LOADER_FACTORY;
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static Function<List<Path>, List<Path>> REMAPPER;

    @Shadow public abstract ClassLoader createLoader(PluginDescriptionFile desc);

    @Redirect(method = "createLoader(Lorg/bukkit/plugin/PluginDescriptionFile;)Ljava/lang/ClassLoader;", at = @At(value = "NEW", target = "java/net/URLClassLoader"))
    private URLClassLoader arclight$useRemapped(URL[] urls, ClassLoader loader, PluginDescriptionFile desc) {
        return fusioncore$create(desc, fusioncore$remap(desc, urls), loader);
    }

    private static URL[] fusioncore$remap(PluginDescriptionFile desc, URL[] urls) {
        if (REMAPPER == null) {
            return urls;
        }
        try {
            List<Path> paths = new ArrayList<>();
            for (URL url : urls) {
                paths.add(Path.of(url.toURI()));
            }
            List<Path> remapped = REMAPPER.apply(paths);
            URL[] out = new URL[remapped.size()];
            for (int i = 0; i < out.length; i++) {
                out[i] = remapped.get(i).toUri().toURL();
            }
            return out;
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException("Could not remap the libraries of " + desc.getName(), e);
        }
    }

    private static URLClassLoader fusioncore$create(PluginDescriptionFile desc, URL[] urls, ClassLoader parent) {
        if (LIBRARY_LOADER_FACTORY != null) {
            return LIBRARY_LOADER_FACTORY.apply(urls, parent);
        }
        return new RemappingURLClassLoader(String.format("%s/L", desc.getName()), urls, parent);
    }

    /** Fusion Core: Paper's {@code createLoader(desc, paperLibraryPaths)}: Spigot's libraries and the given jars in one loader. */
    public ClassLoader createLoader(PluginDescriptionFile desc, List<Path> paperLibraryPaths) {
        ClassLoader spigot = this.createLoader(desc);
        if (paperLibraryPaths == null || paperLibraryPaths.isEmpty()) {
            return spigot;
        }
        List<URL> urls = new ArrayList<>();
        if (spigot instanceof URLClassLoader loader) {
            urls.addAll(List.of(loader.getURLs())); // already remapped
        }
        URL[] extra = new URL[paperLibraryPaths.size()];
        try {
            for (int i = 0; i < extra.length; i++) {
                extra[i] = paperLibraryPaths.get(i).toUri().toURL();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        urls.addAll(List.of(fusioncore$remap(desc, extra)));
        return fusioncore$create(desc, urls.toArray(new URL[0]), this.getClass().getClassLoader());
    }
}
