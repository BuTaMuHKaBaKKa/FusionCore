package io.izzel.arclight.common.mixin.core.world.level.block.entity.api;

import io.izzel.arclight.common.bridge.core.world.level.block.entity.BlockEntityPaperStateBridge;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's configurable sculk sensor range (patch 0665), saved as {@code Paper.ListenerRange}. */
@Mixin(SculkSensorBlockEntity.class)
public abstract class SculkSensorApiMixin implements BlockEntityPaperStateBridge {

    @Shadow public abstract net.minecraft.world.level.gameevent.vibrations.VibrationSystem.User getVibrationUser();

    @Override
    public Integer fusioncore$rangeOverride() {
        return ((BlockEntityPaperStateBridge) this.getVibrationUser()).fusioncore$rangeOverride();
    }

    @Override
    public void fusioncore$setRangeOverride(Integer range) {
        ((BlockEntityPaperStateBridge) this.getVibrationUser()).fusioncore$setRangeOverride(range);
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void fusioncore$loadRange(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        this.fusioncore$setRangeOverride(tag.contains("Paper.ListenerRange") ? tag.getInt("Paper.ListenerRange") : null);
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void fusioncore$saveRange(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        Integer range = this.fusioncore$rangeOverride();
        int own = (Object) this instanceof CalibratedSculkSensorBlockEntity ? 16 : 8;
        if (range != null && range != own) {
            tag.putInt("Paper.ListenerRange", range);
        }
    }
}
