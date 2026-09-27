package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's ItemStack ingredients on {@code ShapelessRecipe} (adding, and removing). */
@Mixin(value = org.bukkit.inventory.ShapelessRecipe.class, remap = false)
public abstract class ShapelessRecipeMixin {

    @Shadow public abstract org.bukkit.inventory.ShapelessRecipe addIngredient(org.bukkit.inventory.RecipeChoice ingredient);

    /** Paper: the stack as an exact ingredient, once per item in it. */
    public org.bukkit.inventory.ShapelessRecipe addIngredient(org.bukkit.inventory.ItemStack item) {
        return this.addIngredient(item.getAmount(), item);
    }

    public org.bukkit.inventory.ShapelessRecipe addIngredient(int count, org.bukkit.inventory.ItemStack item) {
        org.bukkit.inventory.ShapelessRecipe self = (org.bukkit.inventory.ShapelessRecipe) (Object) this;
        com.google.common.base.Preconditions.checkArgument(self.getChoiceList().size() + count <= 9, "Shapeless recipes cannot have more than 9 ingredients");
        while (count-- > 0) {
            org.bukkit.inventory.ItemStack one = item.clone();
            one.setAmount(1);
            this.addIngredient(new org.bukkit.inventory.RecipeChoice.ExactChoice(one));
        }
        return self;
    }

    @Shadow @org.spongepowered.asm.mixin.Final private java.util.List<org.bukkit.inventory.RecipeChoice> ingredients;

    /** Paper: removes one ingredient that accepts the stack. */
    public org.bukkit.inventory.ShapelessRecipe removeIngredient(org.bukkit.inventory.ItemStack item) {
        return this.removeIngredient(1, item);
    }

    /** Paper: removes up to {@code count} ingredients that accept the stack. */
    public org.bukkit.inventory.ShapelessRecipe removeIngredient(int count, org.bukkit.inventory.ItemStack item) {
        java.util.Iterator<org.bukkit.inventory.RecipeChoice> iterator = this.ingredients.iterator();
        while (count > 0 && iterator.hasNext()) {
            org.bukkit.inventory.RecipeChoice choice = iterator.next();
            if (choice.test(item)) {
                iterator.remove();
                count--;
            }
        }
        return (org.bukkit.inventory.ShapelessRecipe) (Object) this;
    }
}
