package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: passes the real hand to {@code BlockPlaceEvent} for Paper's {@code BlockMultiPlaceEvent} constructor. */
@Mixin(value = BlockPlaceEvent.class, remap = false)
public abstract class BlockPlaceEventMixin {

    @Shadow protected EquipmentSlot hand;

    @Inject(method = "<init>(Lorg/bukkit/block/Block;Lorg/bukkit/block/BlockState;Lorg/bukkit/block/Block;Lorg/bukkit/inventory/ItemStack;Lorg/bukkit/entity/Player;ZLorg/bukkit/inventory/EquipmentSlot;)V", at = @At("RETURN"))
    private void fusioncore$takeHand(CallbackInfo ci) {
        EquipmentSlot hand = FusionCoreEvents.takeConstructorArgument(EquipmentSlot.class);
        if (hand != null) {
            this.hand = hand;
        }
    }
}
