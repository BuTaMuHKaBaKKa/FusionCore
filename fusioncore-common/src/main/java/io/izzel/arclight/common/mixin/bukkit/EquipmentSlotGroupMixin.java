package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's {@code EquipmentSlotGroup.BODY}. */
@Mixin(value = EquipmentSlotGroup.class, remap = false)
public abstract class EquipmentSlotGroupMixin {

    @Shadow
    private static EquipmentSlotGroup get(String key, EquipmentSlot slot) {
        throw new AbstractMethodError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void fusioncore$body(CallbackInfo ci) {
        get("body", EquipmentSlot.BODY);
    }
}
