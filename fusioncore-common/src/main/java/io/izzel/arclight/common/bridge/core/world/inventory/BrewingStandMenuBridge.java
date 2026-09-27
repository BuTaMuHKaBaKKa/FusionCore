package io.izzel.arclight.common.bridge.core.world.inventory;

/**
 * Fusion Core: Paper's recipe brew time as a brewing menu sees it (Paper patch 1061 "Add recipeBrewTime"): the stand's own
 * for a real brewing stand, the menu's own for a plugin-made one.
 */
public interface BrewingStandMenuBridge {

    int bridge$getRecipeBrewTime();

    void bridge$setRecipeBrewTime(int recipeBrewTime);
}
