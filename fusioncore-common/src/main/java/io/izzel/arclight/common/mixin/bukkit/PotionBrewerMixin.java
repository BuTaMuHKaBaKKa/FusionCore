package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's custom potion mixes on {@code PotionBrewer} (Paper patch 0669). */
@Mixin(value = org.bukkit.potion.PotionBrewer.class, remap = false)
public interface PotionBrewerMixin {

    default void addPotionMix(io.papermc.paper.potion.PotionMix potionMix) {
        io.izzel.arclight.common.mod.util.FusionCorePotions.addPotionMix(potionMix);
    }

    default void removePotionMix(org.bukkit.NamespacedKey key) {
        io.izzel.arclight.common.mod.util.FusionCorePotions.removePotionMix(key);
    }

    default void resetPotionMixes() {
        io.izzel.arclight.common.mod.util.FusionCorePotions.resetPotionMixes();
    }
}
