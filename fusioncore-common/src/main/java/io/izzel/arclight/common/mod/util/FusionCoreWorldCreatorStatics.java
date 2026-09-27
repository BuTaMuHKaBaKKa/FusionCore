package io.izzel.arclight.common.mod.util;

import org.bukkit.NamespacedKey;
import org.bukkit.WorldCreator;

/**
 * Fusion Core: Paper's statics on {@link WorldCreator} (Paper patch 0500; a mixin cannot add public statics); plugin calls
 * are rewritten here by {@code PaperCallTransformer}.
 */
public final class FusionCoreWorldCreatorStatics {

    private FusionCoreWorldCreatorStatics() {
    }

    public static WorldCreator ofNameAndKey(String levelName, NamespacedKey worldKey) {
        return FusionCoreEvents.newWorldCreator(levelName, worldKey);
    }

    public static WorldCreator ofKey(NamespacedKey worldKey) {
        return FusionCoreEvents.newWorldCreator(worldKey);
    }
}
