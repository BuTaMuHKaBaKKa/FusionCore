package io.papermc.paper.registry.set;

import io.izzel.arclight.common.mod.util.FusionCoreRegistries;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.bukkit.Keyed;

import java.util.ArrayList;
import java.util.List;

/** Fusion Core: Paper's {@code PaperRegistrySets}: registry sets between the API and vanilla. */
public final class PaperRegistrySets {

    @SuppressWarnings("unchecked")
    public static <A extends Keyed, M> HolderSet<M> convertToNms(final ResourceKey<? extends Registry<M>> resourceKey, final RegistryKeySet<A> registryKeySet) { // TODO remove Keyed
        if (registryKeySet instanceof NamedRegistryKeySetImpl<A, ?>) {
            return ((NamedRegistryKeySetImpl<A, M>) registryKeySet).namedSet();
        } else {
            final Registry<M> registry = FusionCoreRegistries.vanillaRegistry(registryKeySet.registryKey());
            final List<Holder<M>> holders = new ArrayList<>();
            for (final TypedKey<A> key : registryKeySet.values()) {
                holders.add(registry.getHolderOrThrow(FusionCoreRegistries.toNms(key)));
            }
            return HolderSet.direct(holders);
        }
    }

    /** Paper's own signature, for registries still being loaded: entries are looked up in {@code lookup}. */
    @SuppressWarnings("unchecked")
    public static <A extends Keyed, M> HolderSet<M> convertToNms(final ResourceKey<? extends Registry<M>> resourceKey, final net.minecraft.resources.RegistryOps.RegistryInfoLookup lookup, final RegistryKeySet<A> registryKeySet) { // TODO remove Keyed
        if (registryKeySet instanceof NamedRegistryKeySetImpl<A, ?>) {
            return ((NamedRegistryKeySetImpl<A, M>) registryKeySet).namedSet();
        } else {
            final net.minecraft.resources.RegistryOps.RegistryInfo<M> registryInfo = lookup.lookup(resourceKey).orElseThrow();
            return HolderSet.direct(key -> {
                return registryInfo.getter().getOrThrow(FusionCoreRegistries.<M>toNms(key));
            }, registryKeySet.values());
        }
    }

    public static <A extends Keyed, M> RegistryKeySet<A> convertToApi(final RegistryKey<A> registryKey, final HolderSet<M> holders) { // TODO remove Keyed
        if (holders instanceof final HolderSet.Named<M> named) {
            return new NamedRegistryKeySetImpl<>(FusionCoreRegistries.fromNms(named.key()), named);
        } else {
            final List<TypedKey<A>> keys = new ArrayList<>();
            for (final Holder<M> holder : holders) {
                if (!(holder instanceof final Holder.Reference<M> reference)) {
                    throw new UnsupportedOperationException("Cannot convert a holder set containing direct holders");
                }
                keys.add(FusionCoreRegistries.typedKey(reference.key()));
            }
            return RegistrySet.keySet(registryKey, keys);
        }
    }

    private PaperRegistrySets() {
    }
}
