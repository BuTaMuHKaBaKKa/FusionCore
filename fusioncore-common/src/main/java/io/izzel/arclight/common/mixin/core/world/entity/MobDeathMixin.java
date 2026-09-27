package io.izzel.arclight.common.mixin.core.world.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityDeathBridge;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: a mob whose death a plugin cancels keeps the equipment it would have dropped (Paper patch 0242). */
@Mixin(Mob.class)
public abstract class MobDeathMixin {

    @WrapOperation(method = "dropCustomDeathLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"))
    private void fusioncore$clearAfterEvent(Mob instance, EquipmentSlot slot, ItemStack stack, Operation<Void> original) {
        if (!((LivingEntityDeathBridge) instance).fusioncore$deferDeathClear(() -> original.call(instance, slot, stack))) {
            original.call(instance, slot, stack);
        }
    }
}
