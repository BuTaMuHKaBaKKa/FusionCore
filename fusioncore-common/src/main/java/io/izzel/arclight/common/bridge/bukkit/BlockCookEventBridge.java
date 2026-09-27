package io.izzel.arclight.common.bridge.bukkit;

/** Fusion Core: the recipe Paper passes to {@code BlockCookEvent} and {@code FurnaceSmeltEvent} (patch 0497). */
public interface BlockCookEventBridge {

    void fusioncore$setRecipe(org.bukkit.inventory.CookingRecipe<?> recipe);
}
