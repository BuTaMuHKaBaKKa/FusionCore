package io.izzel.arclight.common.mixin.bukkit.event;

import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's {@code FurnaceBurnEvent#setConsumeFuel} (API patch 0257, server 0511); the furnace keeps the fuel item when off. */
@Mixin(value = FurnaceBurnEvent.class, remap = false)
public abstract class FurnaceBurnEventMixin implements io.izzel.arclight.common.bridge.bukkit.InventoryEventExtrasBridge.FurnaceBurn {

    @Unique private boolean fusioncore$consumeFuel = true;

    public boolean willConsumeFuel() {
        return this.fusioncore$consumeFuel;
    }

    public void setConsumeFuel(boolean consumeFuel) {
        this.fusioncore$consumeFuel = consumeFuel;
    }

    @Override
    public boolean bridge$willConsumeFuel() {
        return this.fusioncore$consumeFuel;
    }
}
