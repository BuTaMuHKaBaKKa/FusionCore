// Fusion Core: Paper 1.21.1 server class (patch 1014, registry modification), as in Paper; a handler registered after its registry was loaded is reported.
package io.papermc.paper.registry.event.type;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.types.PrioritizableLifecycleEventType;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.event.RegistryEvent;

public final class RegistryLifecycleEventType<T, E extends RegistryEvent<T>> extends PrioritizableLifecycleEventType.Simple<BootstrapContext, E> {

    private final RegistryKey<T> registryKey; // Fusion Core

    public RegistryLifecycleEventType(final RegistryKey<T> registryKey, final String eventName) {
        super(registryKey + " / " + eventName, BootstrapContext.class);
        this.registryKey = registryKey; // Fusion Core
    }

    @Override
    public boolean blocksReloading(final BootstrapContext eventOwner) {
        return false; // only runs once
    }

    // Fusion Core: see PaperRegistryListenerManager#checkLateRegistration
    @Override
    protected void register(final BootstrapContext owner, final io.papermc.paper.plugin.lifecycle.event.handler.configuration.AbstractLifecycleEventHandlerConfiguration<BootstrapContext, E> config) {
        super.register(owner, config);
        io.papermc.paper.registry.PaperRegistryListenerManager.INSTANCE.checkLateRegistration(this.registryKey, owner);
    }
}
