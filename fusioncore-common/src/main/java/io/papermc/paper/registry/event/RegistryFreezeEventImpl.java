// Fusion Core: Paper 1.21.1 server class (patch 1014, registry modification), as in Paper; registry keys go through FusionCoreRegistries.
package io.papermc.paper.registry.event;

import io.papermc.paper.plugin.lifecycle.event.PaperLifecycleEvent;
import io.papermc.paper.registry.RegistryBuilder;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.util.Conversions;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Keyed;
import org.checkerframework.checker.nullness.qual.NonNull;

public record RegistryFreezeEventImpl<T, B extends RegistryBuilder<T>>(
    RegistryKey<T> registryKey,
    WritableRegistry<T, B> registry,
    Conversions conversions
) implements RegistryFreezeEvent<T, B>, PaperLifecycleEvent {

    @Override
    public @NonNull <V extends Keyed> Tag<V> getOrCreateTag(final TagKey<V> tagKey) {
        return RegistryEntryAddEventImpl.getOrCreateTag(this.conversions, tagKey);
    }
}
