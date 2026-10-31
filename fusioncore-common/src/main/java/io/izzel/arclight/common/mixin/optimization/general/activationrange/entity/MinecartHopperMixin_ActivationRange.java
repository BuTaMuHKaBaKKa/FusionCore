package io.izzel.arclight.common.mixin.optimization.general.activationrange.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.izzel.arclight.common.mod.util.FusionCoreActivation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fusion Core: Paper's Entity Activation Range 2.0, a hopper minecart that is switched or picks up items stays active
@Mixin(MinecartHopper.class)
public abstract class MinecartHopperMixin_ActivationRange {

    @Inject(method = "activateMinecart", at = @At("HEAD"))
    private void fusioncore$immunizeOnActivate(int x, int y, int z, boolean powered, CallbackInfo ci) {
        FusionCoreActivation.immunize((Entity) (Object) this, 20);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/MinecartHopper;suckInItems()Z"))
    private boolean fusioncore$immunizeOnPickup(boolean picked) {
        if (picked) {
            FusionCoreActivation.immunize((Entity) (Object) this, 20);
        }
        return picked;
    }
}
