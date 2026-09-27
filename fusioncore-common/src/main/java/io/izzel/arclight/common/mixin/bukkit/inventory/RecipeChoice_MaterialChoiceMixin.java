package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.bukkit.inventory.RecipeChoice;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code RecipeChoice#validate} for a MaterialChoice: no air (API patch "Fix issues with recipe API"). */
@Mixin(value = RecipeChoice.MaterialChoice.class, remap = false)
public abstract class RecipeChoice_MaterialChoiceMixin {

    public RecipeChoice validate(boolean allowEmptyRecipes) {
        if (((RecipeChoice.MaterialChoice) (Object) this).getChoices().stream().anyMatch(org.bukkit.Material::isAir)) {
            throw new IllegalArgumentException("RecipeChoice.MaterialChoice cannot contain air");
        }
        return (RecipeChoice) (Object) this;
    }
}
