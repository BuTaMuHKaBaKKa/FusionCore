package io.papermc.paper.registry;

import io.izzel.arclight.common.mixin.core.world.level.gameevent.GameEventPaperAccessor;
import io.izzel.arclight.common.mod.util.FusionCoreRegistries;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventRunner;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventType;
import io.papermc.paper.registry.data.PaperEnchantmentRegistryEntry;
import io.papermc.paper.registry.data.PaperGameEventRegistryEntry;
import io.papermc.paper.registry.data.util.Conversions;
import io.papermc.paper.registry.event.RegistryEntryAddEventImpl;
import io.papermc.paper.registry.event.RegistryEventMap;
import io.papermc.paper.registry.event.RegistryEventProvider;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import io.papermc.paper.registry.event.RegistryFreezeEventImpl;
import io.papermc.paper.registry.event.WritableRegistry;
import io.papermc.paper.registry.event.type.RegistryEntryAddEventType;
import io.papermc.paper.registry.event.type.RegistryEntryAddEventTypeImpl;
import io.papermc.paper.registry.event.type.RegistryLifecycleEventType;
import com.mojang.serialization.Lifecycle;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gameevent.GameEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fusion Core: Paper's {@code PaperRegistryListenerManager} (patches 1014, 1015): runs the registry events of plugin
 * bootstraps for enchantments and game events. Game events are a built-in registry, frozen before plugins bootstrap,
 * so entries can be changed but not added.
 */
public final class PaperRegistryListenerManager {

    private static final Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final RegistrationInfo FROM_PLUGIN = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());

    public static final PaperRegistryListenerManager INSTANCE = new PaperRegistryListenerManager();

    public final RegistryEventMap valueAddEventTypes = new RegistryEventMap("value add");
    public final RegistryEventMap freezeEventTypes = new RegistryEventMap("freeze");

    /** Registries whose events have run: handlers registered later (a plugin loaded after startup) never run. */
    private final Set<RegistryKey<?>> ran = ConcurrentHashMap.newKeySet();

    private record Entry<M, T, B extends PaperRegistryBuilder<M, T>>(ResourceKey<? extends Registry<M>> mcKey, RegistryKey<T> apiKey,
                                                                    PaperRegistryBuilder.Filler<M, T, B> filler) {
    }

    private static final Map<RegistryKey<?>, Entry<?, ?, ?>> ENTRIES = Map.of(
        RegistryKey.ENCHANTMENT, new Entry<>(Registries.ENCHANTMENT, RegistryKey.ENCHANTMENT, PaperEnchantmentRegistryEntry.PaperBuilder::new),
        RegistryKey.GAME_EVENT, new Entry<>(Registries.GAME_EVENT, RegistryKey.GAME_EVENT, PaperGameEventRegistryEntry.PaperBuilder::new)
    );

    private PaperRegistryListenerManager() {
    }

    @SuppressWarnings("unchecked")
    private static <M, T, B extends PaperRegistryBuilder<M, T>> @Nullable Entry<M, T, B> entry(final ResourceKey<? extends Registry<?>> mcKey) {
        for (final Entry<?, ?, ?> entry : ENTRIES.values()) {
            if (entry.mcKey().equals(mcKey)) {
                return (Entry<M, T, B>) entry;
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface RegisterMethod<M> {

        Holder.Reference<M> register(ResourceKey<M> key, M value, RegistrationInfo registrationInfo);
    }

    /** A data registry entry being loaded: {@code entryAdd} handlers may change it before it is registered. */
    public <M, T, B extends PaperRegistryBuilder<M, T>> Holder.Reference<M> registerWithListeners(
        final Registry<M> registry,
        final ResourceKey<M> key,
        final M nms,
        final RegistrationInfo registrationInfo,
        final @Nullable Conversions conversions,
        final RegisterMethod<M> registerMethod
    ) {
        final @Nullable Entry<M, T, B> entry = entry(registry.key());
        if (entry == null || conversions == null || !this.valueAddEventTypes.hasHandlers(entry.apiKey())) {
            return registerMethod.register(key, nms, registrationInfo);
        }
        final TypedKey<T> typedKey = typedKey(entry, key);
        final B builder = entry.filler().fill(conversions, typedKey, nms);
        return this.registerWithListeners(entry, key, nms, builder, registrationInfo, conversions, registerMethod);
    }

    private <M, T, B extends PaperRegistryBuilder<M, T>> Holder.Reference<M> registerWithListeners(
        final Entry<M, T, B> entry,
        final ResourceKey<M> key,
        final @Nullable M oldNms,
        final B builder,
        RegistrationInfo registrationInfo,
        final Conversions conversions,
        final RegisterMethod<M> registerMethod
    ) {
        if (this.valueAddEventTypes.hasHandlers(entry.apiKey())) {
            final RegistryEntryAddEventImpl<T, B> event = new RegistryEntryAddEventImpl<>(typedKey(entry, key), builder, entry.apiKey(), conversions);
            LifecycleEventRunner.INSTANCE.callEvent(this.valueAddEventTypes.getEventType(entry.apiKey()), event);
        }
        final M newNms = builder.build();
        if (oldNms != null && !newNms.equals(oldNms)) {
            registrationInfo = FROM_PLUGIN;
        }
        return registerMethod.register(key, newNms, registrationInfo);
    }

    /** A data registry about to be frozen: {@code freeze} handlers may add entries to it. */
    public <M, T, B extends PaperRegistryBuilder<M, T>> void runFreezeListeners(final net.minecraft.core.WritableRegistry<M> registry, final @Nullable Conversions conversions) {
        final @Nullable Entry<M, T, B> entry = entry(registry.key());
        if (entry == null) {
            return;
        }
        this.ran.add(entry.apiKey());
        if (conversions == null || !this.freezeEventTypes.hasHandlers(entry.apiKey())) {
            return;
        }
        final WritableRegistry<T, B> writable = (key, value) -> {
            final ResourceKey<M> resourceKey = ResourceKey.create(entry.mcKey(), FusionCoreRegistries.location(key.key()));
            final B builder = entry.filler().fill(conversions, key, null);
            value.accept(builder);
            this.registerWithListeners(entry, resourceKey, null, builder, FROM_PLUGIN, conversions,
                (k, v, info) -> registry.register(k, v, info));
        };
        LifecycleEventRunner.INSTANCE.callEvent(this.freezeEventTypes.getEventType(entry.apiKey()), new RegistryFreezeEventImpl<>(entry.apiKey(), writable, conversions));
    }

    /** Built-in registries, right after the bootstrappers ran (see the class comment). */
    public void runBuiltInListeners() {
        final Entry<GameEvent, org.bukkit.GameEvent, PaperGameEventRegistryEntry.PaperBuilder> entry = entry(Registries.GAME_EVENT);
        if (this.valueAddEventTypes.hasHandlers(RegistryKey.GAME_EVENT)) {
            final List<Holder.Reference<GameEvent>> holders = BuiltInRegistries.GAME_EVENT.holders().toList();
            for (final Holder.Reference<GameEvent> holder : holders) {
                final GameEvent gameEvent = holder.value();
                final PaperGameEventRegistryEntry.PaperBuilder builder = entry.filler().fill(Conversions.BUILT_IN, typedKey(entry, holder.key()), gameEvent);
                LifecycleEventRunner.INSTANCE.callEvent(this.valueAddEventTypes.getEventType(RegistryKey.GAME_EVENT),
                    new RegistryEntryAddEventImpl<>(typedKey(entry, holder.key()), builder, RegistryKey.GAME_EVENT, Conversions.BUILT_IN));
                final int range = builder.build().notificationRadius();
                if (range != gameEvent.notificationRadius()) {
                    ((GameEventPaperAccessor) (Object) gameEvent).fusioncore$setNotificationRadius(range);
                }
            }
        }
        this.ran.add(RegistryKey.GAME_EVENT);
        if (this.freezeEventTypes.hasHandlers(RegistryKey.GAME_EVENT)) {
            final WritableRegistry<org.bukkit.GameEvent, PaperGameEventRegistryEntry.PaperBuilder> writable = (key, value) -> {
                throw new UnsupportedOperationException("Cannot add the game event " + key.key().asString() + ": on NeoForge built-in registries are "
                    + "filled and frozen while mods load, before plugins are bootstrapped; game events are added by mods");
            };
            LifecycleEventRunner.INSTANCE.callEvent(this.freezeEventTypes.getEventType(RegistryKey.GAME_EVENT),
                new RegistryFreezeEventImpl<>(RegistryKey.GAME_EVENT, writable, Conversions.BUILT_IN));
        }
    }

    private static <T> TypedKey<T> typedKey(final Entry<?, T, ?> entry, final ResourceKey<?> key) {
        return TypedKey.create(entry.apiKey(), Key.key(key.location().getNamespace(), key.location().getPath()));
    }

    /** A handler registered for a registry whose events already ran: say so instead of silently never calling it. */
    public void checkLateRegistration(final RegistryKey<?> registryKey, final BootstrapContext owner) {
        if (this.ran.contains(registryKey)) {
            LOGGER.warn("{} registered a handler for registry events of {} after that registry was loaded (the plugin was not bootstrapped "
                + "at startup); it will not be called", owner.getPluginMeta().getName(), registryKey);
        }
    }

    public <T, B extends RegistryBuilder<T>> RegistryEntryAddEventType<T, B> getRegistryValueAddEventType(final RegistryEventProvider<T, B> type) {
        if (!ENTRIES.containsKey(type.registryKey())) {
            throw new IllegalArgumentException(type.registryKey() + " does not support RegistryEntryAddEvent");
        }
        return this.valueAddEventTypes.getOrCreate(type.registryKey(), RegistryEntryAddEventTypeImpl::new);
    }

    public <T, B extends RegistryBuilder<T>> LifecycleEventType.Prioritizable<BootstrapContext, RegistryFreezeEvent<T, B>> getRegistryFreezeEventType(final RegistryEventProvider<T, B> type) {
        if (!ENTRIES.containsKey(type.registryKey())) {
            throw new IllegalArgumentException(type.registryKey() + " does not support RegistryFreezeEvent");
        }
        return this.freezeEventTypes.getOrCreate(type.registryKey(), RegistryLifecycleEventType::new);
    }
}
