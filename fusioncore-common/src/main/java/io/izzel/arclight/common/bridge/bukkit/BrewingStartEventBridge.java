package io.izzel.arclight.common.bridge.bukkit;

/** Fusion Core: the core reads Paper's recipe brew time of {@code BrewingStartEvent} (Paper patch 0491 "Add recipeBrewTime"). */
public interface BrewingStartEventBridge {

    int getRecipeBrewTime();
}
