package io.izzel.arclight.common.bridge.core.world.item.trading;

import org.bukkit.craftbukkit.v.inventory.CraftMerchantRecipe;

public interface MerchantOfferBridge {

    CraftMerchantRecipe bridge$asBukkit();

    /** Fusion Core: Paper's ignore-discounts flag (patch 0425). */
    boolean fusioncore$ignoreDiscounts();

    void fusioncore$setIgnoreDiscounts(boolean ignoreDiscounts);
}
