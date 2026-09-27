package io.izzel.arclight.common.mixin.bukkit.inventory;

import io.izzel.arclight.common.bridge.bukkit.MerchantRecipeBridge;
import io.izzel.arclight.common.bridge.core.world.item.trading.MerchantOfferBridge;
import net.minecraft.world.item.trading.MerchantOffer;
import org.bukkit.craftbukkit.v.inventory.CraftMerchantRecipe;
import org.bukkit.inventory.MerchantRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: Paper's ignore-discounts flag (patch 0425) kept on the offer, and carried over from a plugin's recipe. */
@Mixin(value = CraftMerchantRecipe.class, remap = false)
public abstract class CraftMerchantRecipeMixin extends MerchantRecipeMixin {

    @Shadow @Final private MerchantOffer handle;

    @Override
    public boolean shouldIgnoreDiscounts() {
        return ((MerchantOfferBridge) this.handle).fusioncore$ignoreDiscounts();
    }

    @Override
    public void setIgnoreDiscounts(boolean ignoreDiscounts) {
        ((MerchantOfferBridge) this.handle).fusioncore$setIgnoreDiscounts(ignoreDiscounts);
    }

    @Inject(method = "fromBukkit", at = @At("RETURN"))
    private static void fusioncore$copyIgnoreDiscounts(MerchantRecipe recipe, CallbackInfoReturnable<CraftMerchantRecipe> cir) {
        if (!(recipe instanceof CraftMerchantRecipe)) {
            ((MerchantRecipeBridge) (Object) cir.getReturnValue()).setIgnoreDiscounts(((MerchantRecipeBridge) recipe).shouldIgnoreDiscounts());
        }
    }
}
