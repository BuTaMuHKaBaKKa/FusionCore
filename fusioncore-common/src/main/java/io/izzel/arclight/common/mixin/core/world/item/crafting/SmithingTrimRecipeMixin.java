package io.izzel.arclight.common.mixin.core.world.item.crafting;

import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeBridge;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v.inventory.CraftSmithingTrimRecipe;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SmithingTrimRecipe.class)
public class SmithingTrimRecipeMixin implements RecipeBridge, io.izzel.arclight.common.bridge.core.world.item.crafting.SmithingCopyDataBridge {

    // @formatter:off
    @Shadow @Final Ingredient template;
    @Shadow @Final Ingredient base;
    @Shadow @Final Ingredient addition;
    // @formatter:on

    @Override
    public Recipe bridge$toBukkitRecipe(NamespacedKey id) {
        CraftSmithingTrimRecipe recipe = new CraftSmithingTrimRecipe(id, CraftRecipe.toBukkit(this.template), CraftRecipe.toBukkit(this.base), CraftRecipe.toBukkit(this.addition));
        ((io.izzel.arclight.common.bridge.core.world.item.crafting.SmithingCopyDataBridge) recipe).fusioncore$setCopyDataComponents(this.fusioncore$copyDataComponents);
        return recipe;
    }

    // Fusion Core start: Paper's option to prevent data components copy (patch 0579): without it the trimmed item is a
    // fresh stack of the base item, not a copy of it
    @org.spongepowered.asm.mixin.Unique private boolean fusioncore$copyDataComponents = true;

    @Override
    public boolean fusioncore$copyDataComponents() {
        return this.fusioncore$copyDataComponents;
    }

    @Override
    public void fusioncore$setCopyDataComponents(boolean copyDataComponents) {
        this.fusioncore$copyDataComponents = copyDataComponents;
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
        at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;"))
    private net.minecraft.world.item.ItemStack fusioncore$copyDataComponents(net.minecraft.world.item.ItemStack stack, int count, com.llamalad7.mixinextras.injector.wrapoperation.Operation<net.minecraft.world.item.ItemStack> original) {
        return this.fusioncore$copyDataComponents ? original.call(stack, count) : new net.minecraft.world.item.ItemStack(stack.getItem(), count);
    }
    // Fusion Core end
}
