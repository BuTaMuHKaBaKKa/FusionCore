package io.izzel.arclight.common.mod.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: Paper's {@code PaperRegistryAccess}; every key answers with Bukkit's {@code Registry} constant. */
public final class FusionCoreRegistryAccess implements RegistryAccess {

    public static final FusionCoreRegistryAccess INSTANCE = new FusionCoreRegistryAccess();

    private FusionCoreRegistryAccess() {
    }

    @SuppressWarnings({"unchecked", "removal"})
    @Deprecated(forRemoval = true)
    @Override
    public <T extends Keyed> @Nullable Registry<T> getRegistry(@NotNull Class<T> type) {
        return (Registry<T>) FusionCoreRegistries.bukkitRegistry(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Keyed> @NotNull Registry<T> getRegistry(@NotNull RegistryKey<T> registryKey) {
        return (Registry<T>) FusionCoreRegistries.bukkitRegistry(registryKey);
    }
}
