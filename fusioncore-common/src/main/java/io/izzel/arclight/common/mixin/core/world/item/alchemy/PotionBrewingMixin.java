package io.izzel.arclight.common.mixin.core.world.item.alchemy;

import io.izzel.arclight.common.bridge.core.world.item.alchemy.PotionBrewingBridge;
import io.izzel.arclight.common.mod.util.FusionCorePotions;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: Paper's custom potion mixes (patch 0669), checked before mod recipes. */
@Mixin(PotionBrewing.class)
public class PotionBrewingMixin implements PotionBrewingBridge {

    @Unique
    private final Object2ObjectLinkedOpenHashMap<org.bukkit.NamespacedKey, FusionCorePotions.CustomMix> fusioncore$customMixes = new Object2ObjectLinkedOpenHashMap<>();

    @Override
    public void bridge$addPotionMix(io.papermc.paper.potion.PotionMix mix) {
        if (this.fusioncore$customMixes.containsKey(mix.getKey())) {
            throw new IllegalArgumentException("Duplicate recipe ignored with ID " + mix.getKey());
        }
        this.fusioncore$customMixes.putAndMoveToFirst(mix.getKey(), new FusionCorePotions.CustomMix(mix));
    }

    @Override
    public boolean bridge$removePotionMix(org.bukkit.NamespacedKey key) {
        return this.fusioncore$customMixes.remove(key) != null;
    }

    @Override
    public void bridge$clearPotionMixes() {
        this.fusioncore$customMixes.clear();
    }

    @Inject(method = "isIngredient", cancellable = true, at = @At("HEAD"))
    private void fusioncore$customIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        for (FusionCorePotions.CustomMix mix : this.fusioncore$customMixes.values()) {
            if (mix.ingredient().test(stack)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "isInput", cancellable = true, at = @At("HEAD"))
    private void fusioncore$customInput(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        for (FusionCorePotions.CustomMix mix : this.fusioncore$customMixes.values()) {
            if (mix.input().test(stack)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "hasMix", cancellable = true, at = @At("HEAD"))
    private void fusioncore$customHasMix(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        for (FusionCorePotions.CustomMix mix : this.fusioncore$customMixes.values()) {
            if (mix.input().test(input) && mix.ingredient().test(ingredient)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "mix", cancellable = true, at = @At("HEAD"))
    private void fusioncore$customMix(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (input.isEmpty()) {
            return;
        }
        for (FusionCorePotions.CustomMix mix : this.fusioncore$customMixes.values()) {
            if (mix.input().test(input) && mix.ingredient().test(ingredient)) {
                cir.setReturnValue(mix.result().copy());
                return;
            }
        }
    }
}
