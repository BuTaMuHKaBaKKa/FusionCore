package io.izzel.arclight.common.mixin.core.world.entity.animal.camel;

import io.izzel.arclight.common.mixin.core.world.entity.animal.horse.AbstractHorseMixin;
import net.minecraft.world.entity.animal.camel.Camel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Camel.class)
public abstract class CamelMixin extends AbstractHorseMixin {

    // @formatter:off
    @Shadow public abstract void standUpInstantly();
    // @formatter:on

    // Fusion Core: Paper's EntityToggleSitEvent (patch 0755), only for a real change
    @org.spongepowered.asm.mixin.injection.Inject(method = "sitDown", cancellable = true, at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void fusioncore$sitDownEvent(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (!((net.minecraft.world.entity.animal.camel.Camel) (Object) this).isCamelSitting() && !io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.toggleSit((net.minecraft.world.entity.Entity) (Object) this, true)) {
            ci.cancel();
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "standUp", cancellable = true, at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void fusioncore$standUpEvent(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (((net.minecraft.world.entity.animal.camel.Camel) (Object) this).isCamelSitting() && !io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.toggleSit((net.minecraft.world.entity.Entity) (Object) this, false)) {
            ci.cancel();
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "standUpInstantly", cancellable = true, at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void fusioncore$standUpInstantlyEvent(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (((net.minecraft.world.entity.animal.camel.Camel) (Object) this).isCamelSitting() && !io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.toggleSit((net.minecraft.world.entity.Entity) (Object) this, false)) {
            ci.cancel();
        }
    }
}
