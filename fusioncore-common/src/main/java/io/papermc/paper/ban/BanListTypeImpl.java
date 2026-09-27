// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.ban;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
record BanListTypeImpl<T>(Class<T> typeClass) implements BanListType<T> {
}
