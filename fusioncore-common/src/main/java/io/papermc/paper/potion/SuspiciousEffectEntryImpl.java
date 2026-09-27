// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.potion;

import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
record SuspiciousEffectEntryImpl(PotionEffectType effect, int duration) implements SuspiciousEffectEntry {
}
