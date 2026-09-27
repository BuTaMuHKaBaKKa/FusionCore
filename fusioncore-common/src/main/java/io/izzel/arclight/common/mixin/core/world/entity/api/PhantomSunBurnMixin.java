package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.izzel.arclight.common.bridge.core.world.entity.api.SunBurnBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's {@code shouldBurnInDay} API for Phantom (patch 0528): the sun burn check in {@code aiStep} also needs the flag. */
@Mixin(Phantom.class)
public abstract class PhantomSunBurnMixin implements SunBurnBridge {

    @Unique private boolean fusioncore$shouldBurnInDay = true;

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Phantom;isSunBurnTick()Z"))
    private boolean fusioncore$shouldBurnInDay(boolean original) {
        return this.fusioncore$shouldBurnInDay && original;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$saveBurn(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("Paper.ShouldBurnInDay", this.fusioncore$shouldBurnInDay);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$readBurn(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("Paper.ShouldBurnInDay")) {
            this.fusioncore$shouldBurnInDay = tag.getBoolean("Paper.ShouldBurnInDay");
        }
    }

    @Override
    public boolean fusioncore$shouldBurnInDay() {
        return this.fusioncore$shouldBurnInDay;
    }

    @Override
    public void fusioncore$setShouldBurnInDay(boolean shouldBurnInDay) {
        this.fusioncore$shouldBurnInDay = shouldBurnInDay;
    }
}
