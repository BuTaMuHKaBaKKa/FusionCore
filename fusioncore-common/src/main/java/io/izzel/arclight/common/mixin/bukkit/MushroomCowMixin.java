package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's stew effects on {@link org.bukkit.entity.MushroomCow} (Paper patches 0976 "Suspicious Effect
 * Entry API" and the mushroom cow stew API). The single-effect accessors are Paper's deprecated defaults.
 */
@Mixin(value = org.bukkit.entity.MushroomCow.class, remap = false)
public interface MushroomCowMixin {

    /** Paper: same as Spigot's {@code PotionEffect} overload, which keeps only the type and duration. */
    default boolean addEffectToNextStew(io.papermc.paper.potion.SuspiciousEffectEntry suspiciousEffectEntry, boolean overwrite) {
        com.google.common.base.Preconditions.checkArgument(suspiciousEffectEntry != null, "SuspiciousEffectEntry cannot be null");
        return ((org.bukkit.entity.MushroomCow) this).addEffectToNextStew(
            new org.bukkit.potion.PotionEffect(suspiciousEffectEntry.effect(), suspiciousEffectEntry.duration(), 0), overwrite);
    }

    default java.util.List<io.papermc.paper.potion.SuspiciousEffectEntry> getStewEffects() {
        return io.izzel.arclight.common.mod.util.FusionCorePotions.stewEffects((org.bukkit.craftbukkit.v.entity.CraftMushroomCow) (Object) this);
    }

    default void setStewEffects(java.util.List<io.papermc.paper.potion.SuspiciousEffectEntry> effects) {
        io.izzel.arclight.common.mod.util.FusionCorePotions.setStewEffects((org.bukkit.craftbukkit.v.entity.CraftMushroomCow) (Object) this, effects);
    }

    @Deprecated
    default int getStewEffectDuration() {
        throw new UnsupportedOperationException("Mushroom cows can now hold multiple effects. Use #getStewEffects");
    }

    @Deprecated
    default void setStewEffectDuration(int duration) {
        throw new UnsupportedOperationException("Mushroom cows can now hold multiple effects. Use #setStewEffects");
    }

    @Deprecated
    default org.bukkit.potion.PotionEffectType getStewEffectType() {
        throw new UnsupportedOperationException("Mushroom cows can now hold multiple effects. Use #getStewEffects");
    }

    @Deprecated
    default void setStewEffect(org.bukkit.potion.PotionEffectType type) {
        throw new UnsupportedOperationException("Mushroom cows can now hold multiple effects. Use #setStewEffects");
    }
}
