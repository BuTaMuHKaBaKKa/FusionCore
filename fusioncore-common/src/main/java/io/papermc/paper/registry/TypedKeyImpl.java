// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.registry;

import net.kyori.adventure.key.Key;

record TypedKeyImpl<T>(Key key, RegistryKey<T> registryKey) implements TypedKey<T> {
}
