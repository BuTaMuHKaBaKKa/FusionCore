// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.plugin.lifecycle.event.registrar;

import org.jetbrains.annotations.ApiStatus;

/**
 * To be implemented by types that provide ways to register types
 * either on server start or during a reload
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface Registrar {
}
