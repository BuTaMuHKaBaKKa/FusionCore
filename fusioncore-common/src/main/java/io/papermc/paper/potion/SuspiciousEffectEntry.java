// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.potion;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Contract;

/**
 * Represents a {@link PotionEffectType} paired with a duration.
 */
public sealed interface SuspiciousEffectEntry permits SuspiciousEffectEntryImpl {

    /**
     * Gets the effect type.
     *
     * @return effect type
     */
    PotionEffectType effect();

    /**
     * Gets the duration for this effect instance.
     *
     * @return duration (in ticks) or {@link PotionEffect#INFINITE_DURATION}
     */
    int duration();

    /**
     * Creates a new instance of SuspiciousEffectEntry.
     *
     * @param effectType effect type
     * @param duration duration (in ticks) or {@link PotionEffect#INFINITE_DURATION}
     * @return new instance of an entry
     */
    @Contract(value = "_, _ -> new", pure = true)
    static SuspiciousEffectEntry create(final PotionEffectType effectType, final int duration) {
        return new SuspiciousEffectEntryImpl(effectType, duration);
    }
}
