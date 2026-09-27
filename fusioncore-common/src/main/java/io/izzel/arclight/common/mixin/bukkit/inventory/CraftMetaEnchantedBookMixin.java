package io.izzel.arclight.common.mixin.bukkit.inventory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.izzel.arclight.common.mod.util.FusionCoreEnumConstants;
import org.bukkit.craftbukkit.v.inventory.CraftMetaEnchantedBook;
import org.bukkit.inventory.ItemFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper's {@code ItemFlag.HIDE_STORED_ENCHANTS}. */
@Mixin(value = CraftMetaEnchantedBook.class, remap = false)
public abstract class CraftMetaEnchantedBookMixin {

    @ModifyExpressionValue(method = {"applyToItem", "*(Lnet/minecraft/world/item/enchantment/ItemEnchantments;)V"},
        at = @At(value = "FIELD", target = "Lorg/bukkit/inventory/ItemFlag;HIDE_ADDITIONAL_TOOLTIP:Lorg/bukkit/inventory/ItemFlag;"))
    private ItemFlag fusioncore$storedEnchantsFlag(ItemFlag original) {
        return FusionCoreEnumConstants.ItemFlag$HIDE_STORED_ENCHANTS();
    }
}
