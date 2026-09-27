package io.izzel.arclight.common.mixin.bukkit.inventory;

import io.izzel.arclight.common.bridge.core.world.inventory.BrewingStandMenuBridge;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's recipe brew time on the brewing stand view (Paper patch 1061 "Add recipeBrewTime"). */
@Mixin(value = org.bukkit.inventory.view.BrewingStandView.class, remap = false)
public interface BrewingStandViewMixin {

    default int getRecipeBrewTime() {
        return ((BrewingStandMenuBridge) ((org.bukkit.craftbukkit.v.inventory.CraftInventoryView<?, ?>) (Object) this).getHandle()).bridge$getRecipeBrewTime();
    }

    default void setRecipeBrewTime(int recipeBrewTime) {
        com.google.common.base.Preconditions.checkArgument(recipeBrewTime > 0, "recipeBrewTime must be positive");
        ((BrewingStandMenuBridge) ((org.bukkit.craftbukkit.v.inventory.CraftInventoryView<?, ?>) (Object) this).getHandle()).bridge$setRecipeBrewTime(recipeBrewTime);
    }
}
