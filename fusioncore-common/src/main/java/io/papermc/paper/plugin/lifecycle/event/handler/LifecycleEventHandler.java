// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.plugin.lifecycle.event.handler;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * A handler for a specific event. Can be implemented
 * in a concrete class or as a lambda.
 *
 * @param <E> the event
 */
@ApiStatus.Experimental
@FunctionalInterface
public interface LifecycleEventHandler<E extends LifecycleEvent> {

    void run(E event);
}
