package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.izzel.arclight.common.bridge.core.world.entity.api.FrictionApiBridge;
import net.kyori.adventure.util.TriState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's friction state of living entities (patch 0769): TRUE always has friction, FALSE never. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFrictionApiMixin implements FrictionApiBridge {

    @Unique private TriState fusioncore$frictionState = TriState.NOT_SET;

    @ModifyReturnValue(method = "shouldDiscardFriction", at = @At("RETURN"))
    private boolean fusioncore$frictionState(boolean discard) {
        return !this.fusioncore$frictionState.toBooleanOrElse(!discard);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$saveFrictionState(CompoundTag tag, CallbackInfo ci) {
        FrictionApiBridge.save(this.fusioncore$frictionState, tag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$readFrictionState(CompoundTag tag, CallbackInfo ci) {
        this.fusioncore$frictionState = FrictionApiBridge.read(this.fusioncore$frictionState, tag, this);
    }

    @Override
    public TriState fusioncore$frictionState() {
        return this.fusioncore$frictionState;
    }

    @Override
    public void fusioncore$setFrictionState(TriState state) {
        this.fusioncore$frictionState = state;
    }
}
