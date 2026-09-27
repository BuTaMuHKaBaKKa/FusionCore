// Fusion Core: Paper API 1.21.1 as published in paper-api sources; the jspecify annotations are swapped for JetBrains ones and the
// provider is the core's one directly.
package io.papermc.paper.registry.event;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventType;
import io.papermc.paper.registry.RegistryBuilder;
import io.papermc.paper.registry.event.type.RegistryEntryAddEventType;
import java.util.Optional;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
interface RegistryEventTypeProvider {

    // Fusion Core: the core's provider directly; Paper finds it with ServiceLoader, which does not see the core from a plugin's loader
    Optional<RegistryEventTypeProvider> PROVIDER = Optional.of(new RegistryEventTypeProviderImpl());

    static RegistryEventTypeProvider provider() {
        return PROVIDER.orElseThrow(() -> new IllegalStateException("Could not find a %s service implementation".formatted(RegistryEventTypeProvider.class.getSimpleName())));
    }

    <T, B extends RegistryBuilder<T>> RegistryEntryAddEventType<T, B> registryEntryAdd(RegistryEventProvider<T, B> type);

    <T, B extends RegistryBuilder<T>> LifecycleEventType.Prioritizable<BootstrapContext, RegistryFreezeEvent<T, B>> registryFreeze(RegistryEventProvider<T, B> type);
}
