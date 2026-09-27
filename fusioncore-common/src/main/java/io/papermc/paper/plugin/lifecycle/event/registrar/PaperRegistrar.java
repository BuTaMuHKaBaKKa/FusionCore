// Fusion Core: Paper 1.21.1 server class (patch 0926, lifecycle events), as in Paper.
package io.papermc.paper.plugin.lifecycle.event.registrar;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface PaperRegistrar<O extends LifecycleEventOwner> extends Registrar {

    void setCurrentContext(@Nullable O owner);

    default void invalidate() {
    }
}
