package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.mixin.core.world.entity.api.BoatApiInvoker;
import io.izzel.arclight.common.mod.util.FusionCoreEnumConstants;
import net.minecraft.world.entity.vehicle.Boat;
import org.bukkit.craftbukkit.v.entity.CraftBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: Paper's fix of {@code Boat#getStatus} before the boat's first tick. */
@Mixin(value = CraftBoat.class, remap = false)
public abstract class CraftBoatMixin {

    @Inject(method = "getStatus", cancellable = true, at = @At("HEAD"))
    private void fusioncore$statusBeforeFirstTick(CallbackInfoReturnable<org.bukkit.entity.Boat.Status> cir) {
        Boat handle = ((CraftBoat) (Object) this).getHandle();
        if (handle.status == null) {
            if (((EntityBridge) handle).bridge$isValid()) {
                // don't set the status: it would skew vanilla's old-status check on the next tick
                cir.setReturnValue(CraftBoat.boatStatusFromNms(((BoatApiInvoker) handle).fusioncore$getStatus()));
            } else {
                cir.setReturnValue(FusionCoreEnumConstants.Status$NOT_IN_WORLD());
            }
        }
    }
}
