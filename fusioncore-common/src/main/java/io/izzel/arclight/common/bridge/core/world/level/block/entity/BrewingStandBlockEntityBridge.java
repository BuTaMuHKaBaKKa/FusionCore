package io.izzel.arclight.common.bridge.core.world.level.block.entity;

/** Fusion Core: Paper's recipe brew time of a brewing stand (Paper patch 1061 "Add recipeBrewTime"). Not saved, as in Paper. */
public interface BrewingStandBlockEntityBridge {

    int bridge$getRecipeBrewTime();

    void bridge$setRecipeBrewTime(int recipeBrewTime);
}
