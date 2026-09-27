// Fusion Core: Paper 1.21.1 server class (patch 1014, registry modification). Paper converts components with its
// WrapperAwareSerializer; here they go through vanilla's component codec and Adventure's JSON, against the same lookup.
package io.papermc.paper.registry.data.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.Contract;

import java.util.Optional;

@DefaultQualifier(NonNull.class)
public class Conversions {

    /** Paper's {@code BuiltInRegistries.BUILT_IN_CONVERSIONS}: the built-in registries, for events on them. */
    public static final Conversions BUILT_IN = new Conversions(new RegistryOps.RegistryInfoLookup() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(final ResourceKey<? extends Registry<? extends T>> registryRef) {
            final Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get((ResourceKey) registryRef);
            if (registry == null) {
                return Optional.empty();
            }
            return Optional.of(new RegistryOps.RegistryInfo<>(registry.asLookup(), registry.asTagAddingLookup(), Lifecycle.experimental()));
        }
    });

    private final RegistryOps.RegistryInfoLookup lookup;
    private final RegistryOps<JsonElement> ops;

    public Conversions(final RegistryOps.RegistryInfoLookup lookup) {
        this.lookup = lookup;
        this.ops = RegistryOps.create(JsonOps.INSTANCE, lookup);
    }

    public RegistryOps.RegistryInfoLookup lookup() {
        return this.lookup;
    }

    @Contract("null -> null; !null -> !null")
    public net.minecraft.network.chat.@Nullable Component asVanilla(final @Nullable Component adventure) {
        if (adventure == null) return null;
        return ComponentSerialization.CODEC.parse(this.ops, GsonComponentSerializer.gson().serializeToTree(adventure)).getOrThrow();
    }

    public Component asAdventure(final net.minecraft.network.chat.@Nullable Component vanilla) {
        if (vanilla == null) return Component.empty();
        return GsonComponentSerializer.gson().deserializeFromTree(ComponentSerialization.CODEC.encodeStart(this.ops, vanilla).getOrThrow());
    }
}
