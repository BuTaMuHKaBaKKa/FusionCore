package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreVehicleCollisionEventStatics;
import org.bukkit.event.HandlerList;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/** Fusion Core: the handler list moved up to {@code VehicleCollisionEvent}, as in Paper (API patch 0315). */
@Mixin(value = VehicleEntityCollisionEvent.class, remap = false)
public abstract class VehicleEntityCollisionEventMixin {

    /**
     * @author Fusion Core
     * @reason Paper: the handler list is VehicleCollisionEvent's
     */
    @Overwrite
    public HandlerList getHandlers() {
        return FusionCoreVehicleCollisionEventStatics.HANDLERS;
    }

    /**
     * @author Fusion Core
     * @reason Paper: the handler list is VehicleCollisionEvent's
     */
    @Overwrite
    public static HandlerList getHandlerList() {
        return FusionCoreVehicleCollisionEventStatics.HANDLERS;
    }
}
