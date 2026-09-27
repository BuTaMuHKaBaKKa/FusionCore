package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreVehicleCollisionEventStatics;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.HandlerList;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's pre-collision velocity (API patch 0315, server 0786; Spigot's constructor takes the vehicle's velocity, as
 * Paper's deprecated one), and the handler list moved up to {@code VehicleCollisionEvent}, as in Paper.
 */
@Mixin(value = VehicleBlockCollisionEvent.class, remap = false)
public abstract class VehicleBlockCollisionEventMixin implements io.izzel.arclight.common.bridge.bukkit.VehicleBlockCollisionEventBridge {

    @Unique private Vector fusioncore$velocity;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fusioncore$init(Vehicle vehicle, org.bukkit.block.Block block, CallbackInfo ci) {
        this.fusioncore$velocity = vehicle.getVelocity();
    }

    public Vector getVelocity() {
        return this.fusioncore$velocity;
    }

    @Override
    public void bridge$setVelocity(Vector velocity) {
        this.fusioncore$velocity = velocity;
    }

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
