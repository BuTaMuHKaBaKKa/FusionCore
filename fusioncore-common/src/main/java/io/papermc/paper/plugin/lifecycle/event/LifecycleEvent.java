// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.plugin.lifecycle.event;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base type for all Lifecycle Events.
 * <p>
 * Lifecycle events are generally fired when the older
 * event system is not available, like during early
 * server initialization.
 * @see LifecycleEvents
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface LifecycleEvent {
}
