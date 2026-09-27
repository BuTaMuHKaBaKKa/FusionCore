package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.izzel.arclight.common.bridge.core.world.entity.api.FallingBlockApiBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's {@code FallingBlock#shouldAutoExpire} (patch 0700). */
@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityApiMixin implements FallingBlockApiBridge {

    @Unique private boolean fusioncore$autoExpire = true;

    @ModifyExpressionValue(method = "tick", at = {
        @At(value = "FIELD", opcode = Opcodes.GETFIELD, ordinal = 1, target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;time:I"),
        @At(value = "FIELD", opcode = Opcodes.GETFIELD, ordinal = 2, target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;time:I")})
    private int fusioncore$autoExpire(int time) {
        return this.fusioncore$autoExpire ? time : 0;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$saveAutoExpire(CompoundTag tag, CallbackInfo ci) {
        if (!this.fusioncore$autoExpire) {
            tag.putBoolean("Paper.AutoExpire", false);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$readAutoExpire(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("Paper.AutoExpire")) {
            this.fusioncore$autoExpire = tag.getBoolean("Paper.AutoExpire");
        }
    }

    @Override
    public boolean fusioncore$autoExpire() {
        return this.fusioncore$autoExpire;
    }

    @Override
    public void fusioncore$setAutoExpire(boolean autoExpire) {
        this.fusioncore$autoExpire = autoExpire;
    }
}
