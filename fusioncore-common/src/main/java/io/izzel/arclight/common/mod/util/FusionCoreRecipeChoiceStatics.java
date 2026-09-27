package io.izzel.arclight.common.mod.util;

import org.bukkit.inventory.EmptyRecipeChoice;
import org.bukkit.inventory.RecipeChoice;

/** Fusion Core: Paper's static methods on {@link RecipeChoice}, reached through {@code PaperCallTransformer}. */
public final class FusionCoreRecipeChoiceStatics {

    private FusionCoreRecipeChoiceStatics() {
    }

    /** Paper: an empty choice, allowed only in smithing recipes (API patch "Fix issues with recipe API"). */
    public static RecipeChoice empty() {
        return EmptyRecipeChoice.INSTANCE;
    }
}
