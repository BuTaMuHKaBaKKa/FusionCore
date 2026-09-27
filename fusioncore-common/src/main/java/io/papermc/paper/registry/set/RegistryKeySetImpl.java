// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones. Deviation: calls to Registry methods added by mixins go through FusionCoreRegistries.
package io.papermc.paper.registry.set;

import com.google.common.base.Preconditions;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
record RegistryKeySetImpl<T extends @Nullable Keyed>(RegistryKey<T> registryKey, List<TypedKey<T>> values) implements RegistryKeySet<T> { // TODO remove Keyed

    static <T extends Keyed> RegistryKeySet<T> create(final RegistryKey<T> registryKey, final Iterable<? extends T> values) { // TODO remove Keyed
        final Registry<T> registry = RegistryAccess.registryAccess().getRegistry(registryKey);
        final ArrayList<TypedKey<T>> keys = new ArrayList<>();
        for (final T value : values) {
            final NamespacedKey key = io.izzel.arclight.common.mod.util.FusionCoreRegistries.keyOf(registry, value); // Fusion Core: Registry#getKey comes from a mixin
            Preconditions.checkArgument(key != null, value + " does not have a key in " + registryKey);
            keys.add(TypedKey.create(registryKey, (net.kyori.adventure.key.Key) (Object) key)); // Fusion Core: NamespacedKey is a Key through a mixin
        }
        return new RegistryKeySetImpl<>(registryKey, keys);
    }

    RegistryKeySetImpl {
        values = List.copyOf(values);
    }

    @Override
    public boolean contains(final TypedKey<T> valueKey) {
        return this.values.contains(valueKey);
    }

    @Override
    public Collection<T> resolve(final Registry<T> registry) {
        final List<T> values = new ArrayList<>(this.values.size());
        for (final TypedKey<T> key : this.values) {
            final T value = io.izzel.arclight.common.mod.util.FusionCoreRegistries.get(registry, key.key()); // Fusion Core: Registry#get(Key) comes from a mixin
            Preconditions.checkState(value != null, "Trying to access unbound TypedKey: " + key);
            values.add(value);
        }
        return Collections.unmodifiableList(values);
    }
}
