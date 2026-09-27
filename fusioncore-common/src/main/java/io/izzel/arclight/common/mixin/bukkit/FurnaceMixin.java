package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.CookingRecipe;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

/** Fusion Core: Paper's cook speed multiplier (patch 0247) and used recipes (patch 0664) on {@link Furnace}, on the snapshot. */
@Mixin(value = Furnace.class, remap = false)
public interface FurnaceMixin {

    default double getCookSpeedMultiplier() {
        return FusionCoreBlockStates.cookSpeedMultiplier(this);
    }

    default void setCookSpeedMultiplier(double multiplier) {
        FusionCoreBlockStates.setCookSpeedMultiplier(this, multiplier);
    }

    default int getRecipeUsedCount(NamespacedKey furnaceRecipe) {
        return FusionCoreBlockStates.recipeUsedCount(this, furnaceRecipe);
    }

    default boolean hasRecipeUsedCount(NamespacedKey furnaceRecipe) {
        return FusionCoreBlockStates.hasRecipeUsedCount(this, furnaceRecipe);
    }

    default void setRecipeUsedCount(CookingRecipe<?> furnaceRecipe, int count) {
        FusionCoreBlockStates.setRecipeUsedCount(this, furnaceRecipe, count);
    }

    default void setRecipesUsed(Map<CookingRecipe<?>, Integer> recipesUsed) {
        FusionCoreBlockStates.setRecipesUsed(this, recipesUsed);
    }
}
