// Fusion Core: Paper 1.21.1 server class (patch 1014, registry modification), as in Paper; registry keys go through FusionCoreRegistries.
package io.papermc.paper.registry.event;

import io.izzel.arclight.common.mod.util.FusionCoreRegistries;
import io.papermc.paper.plugin.lifecycle.event.PaperLifecycleEvent;
import io.papermc.paper.registry.RegistryBuilder;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.util.Conversions;
import io.papermc.paper.registry.set.NamedRegistryKeySetImpl;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.RegistryOps;
import org.bukkit.Keyed;
import org.checkerframework.checker.nullness.qual.NonNull;

public record RegistryEntryAddEventImpl<T, B extends RegistryBuilder<T>>(
    TypedKey<T> key,
    B builder,
    RegistryKey<T> registryKey,
    Conversions conversions
) implements RegistryEntryAddEvent<T, B>, PaperLifecycleEvent {

    @Override
    public @NonNull <V extends Keyed> Tag<V> getOrCreateTag(final TagKey<V> tagKey) {
        return getOrCreateTag(this.conversions, tagKey);
    }

    static <V extends Keyed> Tag<V> getOrCreateTag(final Conversions conversions, final TagKey<V> tagKey) {
        final RegistryOps.RegistryInfo<Object> registryInfo = conversions.lookup().lookup(FusionCoreRegistries.toNms(tagKey.registryKey())).orElseThrow();
        final HolderSet.Named<?> tagSet = registryInfo.getter().getOrThrow(FusionCoreRegistries.toNms(tagKey));
        return new NamedRegistryKeySetImpl<>(tagKey, tagSet);
    }
}
