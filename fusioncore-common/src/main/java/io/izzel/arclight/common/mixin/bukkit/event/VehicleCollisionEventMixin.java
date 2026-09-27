package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreVehicleCollisionEventStatics;
import org.bukkit.event.HandlerList;
import org.bukkit.event.vehicle.VehicleCollisionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's own handler list on {@code VehicleCollisionEvent} (API patch 0315). */
@Mixin(value = VehicleCollisionEvent.class, remap = false)
public abstract class VehicleCollisionEventMixin {

    @Unique
    private static HandlerList getHandlerList() {
        return FusionCoreVehicleCollisionEventStatics.HANDLERS;
    }

    public HandlerList getHandlers() {
        return FusionCoreVehicleCollisionEventStatics.HANDLERS;
    }
}
