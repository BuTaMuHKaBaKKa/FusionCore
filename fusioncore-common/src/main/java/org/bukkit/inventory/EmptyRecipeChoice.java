package org.bukkit.inventory;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/** Fusion Core: Paper's empty recipe choice, the value of {@code RecipeChoice.empty()}. */
@ApiStatus.Internal
public record EmptyRecipeChoice() implements RecipeChoice {

    public static final RecipeChoice INSTANCE = new EmptyRecipeChoice();

    @Override
    @Deprecated
    public @NotNull ItemStack getItemStack() {
        throw new UnsupportedOperationException("This is an empty RecipeChoice");
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public @NotNull RecipeChoice clone() {
        return this;
    }

    @Override
    public boolean test(final @NotNull ItemStack itemStack) {
        return false;
    }

    public @NotNull RecipeChoice validate(final boolean allowEmptyRecipes) {
        if (allowEmptyRecipes) return this;
        throw new IllegalArgumentException("empty RecipeChoice isn't allowed here");
    }
}
