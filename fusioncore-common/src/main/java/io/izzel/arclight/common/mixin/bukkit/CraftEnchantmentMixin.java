package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEnchantments;
import org.bukkit.craftbukkit.v.enchantments.CraftEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fusion Core: Paper's {@code isTreasure} (patch "More Enchantment API"): vanilla's {@code #minecraft:treasure} tag. Spigot
 * counts every enchantment missing from the enchanting table as treasure, including ones a datapack or mod keeps out of it.
 */
@Mixin(value = CraftEnchantment.class, remap = false)
public abstract class CraftEnchantmentMixin {

    @Inject(method = "isTreasure", cancellable = true, at = @At("HEAD"))
    private void fusioncore$treasureTag(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(FusionCoreEnchantments.isTreasure((CraftEnchantment) (Object) this));
    }
}
