package io.izzel.arclight.common.bridge.bukkit;

/** Fusion Core: Paper's world key and keepSpawnLoaded on {@code WorldCreator} ({@code WorldCreatorMixin}). */
public interface WorldCreatorBridge {

    void fusioncore$setKey(org.bukkit.NamespacedKey key);

    org.bukkit.NamespacedKey fusioncore$key();

    net.kyori.adventure.util.TriState fusioncore$keepSpawnLoaded();
}
