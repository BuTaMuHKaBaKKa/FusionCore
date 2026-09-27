package io.izzel.arclight.common.mixin.bukkit.inventory;

import io.izzel.arclight.common.bridge.bukkit.MerchantRecipeBridge;
import org.bukkit.inventory.MerchantRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's ignore-discounts flag on {@link MerchantRecipe} (patch 0425). */
@Mixin(value = MerchantRecipe.class, remap = false)
public abstract class MerchantRecipeMixin implements MerchantRecipeBridge {

    @Unique private boolean fusioncore$ignoreDiscounts;

    @Override
    public boolean shouldIgnoreDiscounts() {
        return this.fusioncore$ignoreDiscounts;
    }

    @Override
    public void setIgnoreDiscounts(boolean ignoreDiscounts) {
        this.fusioncore$ignoreDiscounts = ignoreDiscounts;
    }
}
