package io.izzel.arclight.common.mixin.bukkit.inventory;

import io.izzel.arclight.common.bridge.core.world.item.crafting.SmithingCopyDataBridge;
import org.bukkit.craftbukkit.v.inventory.CraftSmithingTransformRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: Paper's "copy data components" flag (patch 0579) carried from a plugin's recipe to the vanilla one. */
@Mixin(value = CraftSmithingTransformRecipe.class, remap = false)
public abstract class CraftSmithingTransformRecipeMixin {

    @Inject(method = "fromBukkitRecipe", at = @At("RETURN"))
    private static void fusioncore$copyFlag(SmithingTransformRecipe recipe, CallbackInfoReturnable<CraftSmithingTransformRecipe> cir) {
        if (cir.getReturnValue() != recipe) {
            ((SmithingCopyDataBridge) cir.getReturnValue()).fusioncore$setCopyDataComponents(((SmithingCopyDataBridge) recipe).fusioncore$copyDataComponents());
        }
    }

    @ModifyArg(method = "addToCraftingManager", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeHolder;<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/item/crafting/Recipe;)V"))
    private net.minecraft.world.item.crafting.Recipe<?> fusioncore$passFlag(net.minecraft.world.item.crafting.Recipe<?> recipe) {
        ((SmithingCopyDataBridge) recipe).fusioncore$setCopyDataComponents(((SmithingCopyDataBridge) this).fusioncore$copyDataComponents());
        return recipe;
    }
}
