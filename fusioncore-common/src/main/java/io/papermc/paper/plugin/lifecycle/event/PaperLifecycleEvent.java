// Fusion Core: Paper 1.21.1 server class (patch 0926, lifecycle events), as in Paper.
package io.papermc.paper.plugin.lifecycle.event;

public interface PaperLifecycleEvent extends LifecycleEvent {

    // called after all handlers have been run. Can be
    // used to invalid various contexts to plugins can't
    // try to re-use them by storing them from the event
    default void invalidate() {
    }
}
