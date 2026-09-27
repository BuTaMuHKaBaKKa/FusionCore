package io.izzel.arclight.common.mixin.core.world.item.crafting;

import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeBridge;
import io.izzel.arclight.common.mod.util.ArclightSpecialRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v.inventory.CraftSmithingTransformRecipe;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin implements RecipeBridge, io.izzel.arclight.common.bridge.core.world.item.crafting.SmithingCopyDataBridge {

    // @formatter:off
    @Shadow @Final ItemStack result;
    @Shadow @Final Ingredient template;
    @Shadow @Final Ingredient base;
    @Shadow @Final Ingredient addition;
    // @formatter:on

    @Override
    public Recipe bridge$toBukkitRecipe(NamespacedKey id) {
        if (this.result.isEmpty()) {
            return new ArclightSpecialRecipe(id, (SmithingTransformRecipe) (Object) this);
        }
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);

        CraftSmithingTransformRecipe recipe = new CraftSmithingTransformRecipe(id, result, CraftRecipe.toBukkit(this.template), CraftRecipe.toBukkit(this.base), CraftRecipe.toBukkit(this.addition));
        ((io.izzel.arclight.common.bridge.core.world.item.crafting.SmithingCopyDataBridge) recipe).fusioncore$setCopyDataComponents(this.fusioncore$copyDataComponents);
        return recipe;
    }

    // Fusion Core start: Paper's option to prevent data components copy (patch 0579): without it the result's own
    // components are not applied over the base item's
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
        at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;applyComponents(Lnet/minecraft/core/component/DataComponentPatch;)V"))
    private void fusioncore$copyDataComponents(ItemStack stack, net.minecraft.core.component.DataComponentPatch patch, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        if (this.fusioncore$copyDataComponents) {
            original.call(stack, patch);
        }
    }
    // Fusion Core end
}
