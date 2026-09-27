// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.registry;

import org.jetbrains.annotations.ApiStatus;

/**
 * To be implemented by any type used for modifying registries.
 *
 * @param <T> registry value type
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface RegistryBuilder<T> {
}
