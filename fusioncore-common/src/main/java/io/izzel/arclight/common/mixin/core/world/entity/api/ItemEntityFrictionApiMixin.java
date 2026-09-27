package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.world.entity.api.FrictionApiBridge;
import net.kyori.adventure.util.TriState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's friction state of items (patch 0769): FALSE keeps the horizontal speed, on the ground too. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityFrictionApiMixin implements FrictionApiBridge {

    @Unique private TriState fusioncore$frictionState = TriState.NOT_SET;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 fusioncore$frictionState(Vec3 motion, double x, double y, double z, Operation<Vec3> original) {
        if (this.fusioncore$frictionState == TriState.FALSE) {
            return original.call(motion, 1.0D, y, 1.0D);
        }
        return original.call(motion, x, y, z);
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
