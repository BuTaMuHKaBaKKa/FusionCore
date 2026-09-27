package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCloseEvent$Reason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's inventory close reason (patch 0117); UNKNOWN when none was given. */
@Mixin(value = InventoryCloseEvent.class, remap = false)
public abstract class InventoryCloseEventMixin implements io.izzel.arclight.common.bridge.bukkit.InventoryCloseEventBridge {

    @Unique private InventoryCloseEvent$Reason fusioncore$reason;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fusioncore$reason(CallbackInfo ci) {
        this.fusioncore$reason = FusionCoreEvents.closeReason();
    }

    @Override
    public void fusioncore$setReason(InventoryCloseEvent$Reason reason) {
        this.fusioncore$reason = reason;
    }

    public InventoryCloseEvent$Reason getReason() {
        return this.fusioncore$reason;
    }
}
