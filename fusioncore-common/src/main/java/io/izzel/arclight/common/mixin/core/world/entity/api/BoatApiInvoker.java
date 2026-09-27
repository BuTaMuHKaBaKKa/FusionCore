package io.izzel.arclight.common.mixin.core.world.entity.api;

import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public Boat methods Paper's entity API calls. */
@Mixin(Boat.class)
public interface BoatApiInvoker {

    @Invoker("getStatus")
    Boat.Status fusioncore$getStatus();
}
