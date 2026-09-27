// Fusion Core: Paper API 1.21.1 as published in paper-api sources; the jspecify annotations are swapped for JetBrains ones, the provider is
// the core's one directly.
package io.papermc.paper.plugin.lifecycle.event.types;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import java.util.Optional;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
interface LifecycleEventTypeProvider {

    // Fusion Core: the core's provider directly; Paper finds it with ServiceLoader, which does not see the core from a plugin's loader
    Optional<LifecycleEventTypeProvider> INSTANCE = Optional.of(new LifecycleEventTypeProviderImpl());

    static LifecycleEventTypeProvider provider() {
        return INSTANCE.orElseThrow();
    }

    <O extends LifecycleEventOwner, E extends LifecycleEvent> LifecycleEventType.Monitorable<O, E> monitor(String name, Class<? extends O> ownerType);

    <O extends LifecycleEventOwner, E extends LifecycleEvent> LifecycleEventType.Prioritizable<O, E> prioritized(String name, Class<? extends O> ownerType);

    TagEventTypeProvider tagProvider();
}
