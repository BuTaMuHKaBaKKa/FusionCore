package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's recipe brew time on the brewing stand block state (Paper patch 1061 "Add recipeBrewTime"). Like
 * Paper, it lives on the state's snapshot and is not saved, so {@code update()} does not carry it to the world.
 */
@Mixin(value = org.bukkit.block.BrewingStand.class, remap = false)
public interface BrewingStandMixin {

    default int getRecipeBrewTime() {
        return ((io.izzel.arclight.common.bridge.core.world.level.block.entity.BrewingStandBlockEntityBridge) ((CraftBlockEntityStateAccessor) (Object) this).fusioncore$getSnapshot()).bridge$getRecipeBrewTime();
    }

    default void setRecipeBrewTime(int recipeBrewTime) {
        com.google.common.base.Preconditions.checkArgument(recipeBrewTime > 0, "recipeBrewTime must be positive");
        ((io.izzel.arclight.common.bridge.core.world.level.block.entity.BrewingStandBlockEntityBridge) ((CraftBlockEntityStateAccessor) (Object) this).fusioncore$getSnapshot()).bridge$setRecipeBrewTime(recipeBrewTime);
    }
}
