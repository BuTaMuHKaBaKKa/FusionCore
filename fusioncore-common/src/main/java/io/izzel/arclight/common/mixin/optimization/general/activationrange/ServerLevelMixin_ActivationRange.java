package io.izzel.arclight.common.mixin.optimization.general.activationrange;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.optimization.EntityBridge_ActivationRange;
import io.izzel.arclight.common.mod.util.FusionCoreActivation;
import io.izzel.arclight.i18n.ArclightConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spigotmc.ActivationRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin_ActivationRange implements FusionCoreActivation.LevelState {

    // @formatter:off
    @Shadow private void tickPassenger(Entity vehicle, Entity passenger) { throw new AssertionError(); }
    // @formatter:on

    @Unique
    private static final boolean arclight$applyInactive = ArclightConfig.spec().getOptimization().useActivationAndTrackingRange();

    // Fusion Core: Paper's wake-up-inactive budget of this level (animals, flying monsters, monsters, villagers)
    @Unique private final int[] fusioncore$wakeupRemaining = new int[4];

    @Override
    public int[] fusioncore$wakeupRemaining() {
        return this.fusioncore$wakeupRemaining;
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;entityTickList:Lnet/minecraft/world/level/entity/EntityTickList;"))
    private void activationRange$activateEntity(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        ActivationRange.activateEntities((ServerLevel) (Object) this);
    }

    @Inject(method = "tickNonPassenger", cancellable = true, at = @At(value = "HEAD"))
    private void activationRange$inactiveTick(Entity entityIn, CallbackInfo ci) {
        if (arclight$applyInactive && !ActivationRange.checkIfActive(entityIn)) {
            ++entityIn.tickCount;
            if (((EntityBridge) entityIn).bridge$forge$canUpdate()) {
                ((EntityBridge_ActivationRange) entityIn).bridge$inactiveTick();
            }
            // Fusion Core: Paper's Entity Activation Range 2.0, the passengers of an inactive vehicle still tick, each by its
            // own activation (was: frozen with the vehicle)
            for (Entity passenger : entityIn.getPassengers()) {
                this.tickPassenger(entityIn, passenger);
            }
            ci.cancel();
        }
    }

    // Fusion Core: Paper's Entity Activation Range 2.0, an inactive passenger only gets its inactive tick and keeps its seat
    @WrapOperation(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;rideTick()V"))
    private void fusioncore$inactivePassenger(Entity passenger, Operation<Void> original,
                                             @com.llamalad7.mixinextras.sugar.Local(argsOnly = true, ordinal = 0) Entity vehicle) {
        if (!arclight$applyInactive || ActivationRange.checkIfActive(passenger)) {
            original.call(passenger);
            return;
        }
        passenger.setDeltaMovement(Vec3.ZERO);
        ((EntityBridge_ActivationRange) passenger).bridge$inactiveTick();
        vehicle.positionRider(passenger);
    }
}
