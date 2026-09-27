package io.izzel.arclight.common.mixin.bukkit.inventory;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code ShapedRecipe#setIngredient(char, ItemStack)} (API patch "Add ItemStack Recipe API helper methods"). */
@Mixin(value = ShapedRecipe.class, remap = false)
public abstract class ShapedRecipeMixin {

    @Shadow public abstract ShapedRecipe setIngredient(char key, RecipeChoice ingredient);

    /** Paper: the stack as an exact ingredient. */
    public ShapedRecipe setIngredient(char key, ItemStack item) {
        Preconditions.checkArgument(!item.getType().isAir(), "Item cannot be air");
        return this.setIngredient(key, new RecipeChoice.ExactChoice(item.clone()));
    }
}
