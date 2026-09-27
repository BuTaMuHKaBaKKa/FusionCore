package io.papermc.paper.registry.set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import io.izzel.arclight.common.mod.util.FusionCoreRegistries;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Set;

/**
 * Fusion Core: Paper's {@code NamedRegistryKeySetImpl}: a vanilla tag as Paper's {@link Tag} and Bukkit's {@link org.bukkit.Tag}.
 */
public record NamedRegistryKeySetImpl<T extends Keyed, M>( // TODO remove Keyed
    TagKey<T> tagKey,
    HolderSet.Named<M> namedSet
) implements Tag<T>, org.bukkit.Tag<T> {

    @Override
    public @Unmodifiable Collection<TypedKey<T>> values() {
        final ImmutableList.Builder<TypedKey<T>> builder = ImmutableList.builder();
        for (final Holder<M> holder : this.namedSet) {
            builder.add(FusionCoreRegistries.typedKey(((Holder.Reference<?>) holder).key()));
        }
        return builder.build();
    }

    @Override
    public RegistryKey<T> registryKey() {
        return this.tagKey.registryKey();
    }

    @Override
    public boolean contains(final TypedKey<T> valueKey) {
        return Iterables.any(this.namedSet, h -> {
            return FusionCoreRegistries.typedKey(((Holder.Reference<?>) h).key()).equals(valueKey);
        });
    }

    @Override
    public @Unmodifiable Collection<T> resolve(final Registry<T> registry) {
        final ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (final Holder<M> holder : this.namedSet) {
            final net.minecraft.resources.ResourceLocation location = ((Holder.Reference<?>) holder).key().location();
            builder.add(registry.getOrThrow(new NamespacedKey(location.getNamespace(), location.getPath())));
        }
        return builder.build();
    }

    @Override
    public boolean isTagged(final @NotNull T item) {
        return this.getValues().contains(item);
    }

    @Override
    public @NotNull Set<T> getValues() {
        return Set.copyOf(this.resolve(RegistryAccess.registryAccess().getRegistry(this.registryKey())));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        final Key key = this.tagKey().key();
        return new NamespacedKey(key.namespace(), key.value());
    }
}
