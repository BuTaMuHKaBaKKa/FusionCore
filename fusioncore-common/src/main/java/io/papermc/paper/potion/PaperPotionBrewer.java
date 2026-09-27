package io.papermc.paper.potion;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

/** Fusion Core: Paper's {@code PaperPotionBrewer} (patch 0669). */
public final class PaperPotionBrewer implements PotionBrewer {

    public static final PaperPotionBrewer INSTANCE = new PaperPotionBrewer();

    private PaperPotionBrewer() {
    }

    @Override
    @Deprecated
    public PotionEffect createEffect(PotionEffectType potion, int duration, int amplifier) {
        return potion.createEffect(duration, amplifier);
    }

    @Override
    @Deprecated
    public Collection<PotionEffect> getEffectsFromDamage(int damage) {
        return new ArrayList<>();
    }

    @Override
    @Deprecated
    public Collection<PotionEffect> getEffects(PotionType type, boolean upgraded, boolean extended) {
        final org.bukkit.NamespacedKey key = type.getKey();

        Preconditions.checkArgument(!key.getKey().startsWith("strong_"), "Strong potion type cannot be used directly, got %s", key);
        Preconditions.checkArgument(!key.getKey().startsWith("long_"), "Extended potion type cannot be used directly, got %s", key);

        org.bukkit.NamespacedKey effectiveKey = key;
        if (upgraded) {
            effectiveKey = new org.bukkit.NamespacedKey(key.getNamespace(), "strong_" + key.getKey());
        } else if (extended) {
            effectiveKey = new org.bukkit.NamespacedKey(key.getNamespace(), "long_" + key.getKey());
        }

        final PotionType effectivePotionType = org.bukkit.Registry.POTION.get(effectiveKey);
        Preconditions.checkNotNull(effectivePotionType, "Unknown potion type from data " + effectiveKey); // Legacy error message in 1.20.4
        return effectivePotionType.getPotionEffects();
    }
}
