package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.bukkit.inventory.RecipeChoice;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code RecipeChoice#validate} for a ExactChoice: no air (API patch "Fix issues with recipe API"). */
@Mixin(value = RecipeChoice.ExactChoice.class, remap = false)
public abstract class RecipeChoice_ExactChoiceMixin {

    public RecipeChoice validate(boolean allowEmptyRecipes) {
        if (((RecipeChoice.ExactChoice) (Object) this).getChoices().stream().anyMatch(s -> s.getType().isAir())) {
            throw new IllegalArgumentException("RecipeChoice.ExactChoice cannot contain air");
        }
        return (RecipeChoice) (Object) this;
    }
}
