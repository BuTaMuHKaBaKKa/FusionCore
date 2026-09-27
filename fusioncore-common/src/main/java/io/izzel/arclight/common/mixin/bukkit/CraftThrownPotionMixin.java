package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.craftbukkit.v.entity.CraftThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's {@code setItem} (Paper patch 0674 "More Projectile API"): any item may be thrown, not only splash
 * and lingering potions; a non-potion item keeps the current potion meta.
 */
@Mixin(value = CraftThrownPotion.class, remap = false)
public abstract class CraftThrownPotionMixin {

    @Inject(method = "setItem", cancellable = true, at = @At("HEAD"))
    private void fusioncore$setItem(ItemStack item, CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCorePotions.setItem(((CraftThrownPotion) (Object) this).getHandle(), item);
        ci.cancel();
    }
}
