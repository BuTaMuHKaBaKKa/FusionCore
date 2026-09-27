package io.izzel.arclight.common.mod.util;

/**
 * Fusion Core: Paper's static methods on {@link org.bukkit.UnsafeValues}; a mixin cannot add public statics, so
 * {@code PaperCallTransformer} points plugin calls here.
 */
public final class FusionCoreUnsafeValuesStatics {

    private FusionCoreUnsafeValuesStatics() {
    }

    /** Paper: a plugin whose api-version this server does not support. */
    @Deprecated(forRemoval = true)
    public static boolean isLegacyPlugin(org.bukkit.plugin.Plugin plugin) {
        return !FusionCoreUnsafeValues.isSupportedApiVersion(plugin.getDescription().getAPIVersion());
    }
}
