package io.izzel.arclight.common.bridge.bukkit;

/** Fusion Core: Paper's ignore-discounts flag on {@code MerchantRecipe} (patch 0425), for core code javac cannot show it to. */
public interface MerchantRecipeBridge {

    boolean shouldIgnoreDiscounts();

    void setIgnoreDiscounts(boolean ignoreDiscounts);
}
