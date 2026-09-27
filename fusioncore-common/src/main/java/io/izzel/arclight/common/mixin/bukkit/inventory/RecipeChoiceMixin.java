package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.bukkit.inventory.RecipeChoice;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code RecipeChoice#validate} (API patch "Fix issues with recipe API"), internal API. */
@Mixin(value = RecipeChoice.class, remap = false)
public interface RecipeChoiceMixin {

    default RecipeChoice validate(boolean allowEmptyRecipes) {
        return (RecipeChoice) this;
    }
}
