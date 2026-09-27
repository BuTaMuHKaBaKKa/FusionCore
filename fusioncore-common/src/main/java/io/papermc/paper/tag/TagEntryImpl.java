// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.tag;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
record TagEntryImpl<T>(Key key, boolean isTag, boolean isRequired) implements TagEntry<T> {
}
