package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's hand of {@code BlockCanBuildEvent} (Paper patch 0870); HAND unless built by {@code FusionCoreEvents.newBlockCanBuildEvent}. */
@Mixin(value = BlockCanBuildEvent.class, remap = false)
public abstract class BlockCanBuildEventMixin {

    private EquipmentSlot fusioncore$hand = EquipmentSlot.HAND;

    @Inject(method = "<init>(Lorg/bukkit/block/Block;Lorg/bukkit/entity/Player;Lorg/bukkit/block/data/BlockData;Z)V", at = @At("RETURN"))
    private void fusioncore$takeHand(CallbackInfo ci) {
        EquipmentSlot hand = FusionCoreEvents.takeConstructorArgument(EquipmentSlot.class);
        if (hand != null) {
            this.fusioncore$hand = hand;
        }
    }

    public EquipmentSlot getHand() {
        return this.fusioncore$hand;
    }
}
