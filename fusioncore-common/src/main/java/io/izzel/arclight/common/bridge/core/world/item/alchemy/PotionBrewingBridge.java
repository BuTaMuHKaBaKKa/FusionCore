package io.izzel.arclight.common.bridge.core.world.item.alchemy;

/** Fusion Core: Paper's custom potion mixes kept on the brewing registry (Paper patch 0669). */
public interface PotionBrewingBridge {

    /** @throws IllegalArgumentException when a mix with the same key is already registered */
    void bridge$addPotionMix(io.papermc.paper.potion.PotionMix mix);

    boolean bridge$removePotionMix(org.bukkit.NamespacedKey key);

    void bridge$clearPotionMixes();
}
