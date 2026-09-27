// Fusion Core: Paper 1.21.1 server class (patch 1014, registry modification), as in Paper; a handler registered after its registry was loaded is reported.
package io.papermc.paper.registry.event.type;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.types.PrioritizableLifecycleEventType;
import io.papermc.paper.registry.RegistryBuilder;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.event.RegistryEntryAddEvent;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RegistryEntryAddEventTypeImpl<T, B extends RegistryBuilder<T>> extends PrioritizableLifecycleEventType<BootstrapContext, RegistryEntryAddEvent<T, B>, RegistryEntryAddConfiguration<T>> implements RegistryEntryAddEventType<T, B> {

    private final RegistryKey<T> registryKey; // Fusion Core

    public RegistryEntryAddEventTypeImpl(final RegistryKey<T> registryKey, final String eventName) {
        super(registryKey + " / " + eventName, BootstrapContext.class);
        this.registryKey = registryKey; // Fusion Core
    }

    @Override
    public boolean blocksReloading(final BootstrapContext eventOwner) {
        return false; // only runs once
    }

    @Override
    public RegistryEntryAddConfiguration<T> newHandler(final LifecycleEventHandler<? super RegistryEntryAddEvent<T, B>> handler) {
        return new RegistryEntryAddHandlerConfiguration<>(handler, this);
    }

    @Override
    public void forEachHandler(final RegistryEntryAddEvent<T, B> event, final Consumer<RegisteredHandler<BootstrapContext, RegistryEntryAddEvent<T, B>>> consumer, final Predicate<RegisteredHandler<BootstrapContext, RegistryEntryAddEvent<T, B>>> predicate) {
        super.forEachHandler(event, consumer, predicate.and(handler -> this.matchesTarget(event, handler)));
    }

    private boolean matchesTarget(final RegistryEntryAddEvent<T, B> event, final RegisteredHandler<BootstrapContext, RegistryEntryAddEvent<T, B>> handler) {
        final RegistryEntryAddHandlerConfiguration<T, B> config = (RegistryEntryAddHandlerConfiguration<T, B>) handler.config();
        return config.filter() == null || config.filter().test(event.key());
    }

    // Fusion Core: see PaperRegistryListenerManager#checkLateRegistration
    @Override
    protected void register(final BootstrapContext owner, final io.papermc.paper.plugin.lifecycle.event.handler.configuration.AbstractLifecycleEventHandlerConfiguration<BootstrapContext, RegistryEntryAddEvent<T, B>> config) {
        super.register(owner, config);
        io.papermc.paper.registry.PaperRegistryListenerManager.INSTANCE.checkLateRegistration(this.registryKey, owner);
    }
}
