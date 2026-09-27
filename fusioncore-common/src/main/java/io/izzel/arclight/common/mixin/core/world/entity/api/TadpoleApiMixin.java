package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.izzel.arclight.common.bridge.core.world.entity.api.TadpoleApiBridge;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's tadpole age lock (patch 0557): no aging per tick or by feeding, kept in saves and buckets. */
@Mixin(Tadpole.class)
public abstract class TadpoleApiMixin implements TadpoleApiBridge {

    @Unique private boolean fusioncore$ageLocked;

    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/frog/Tadpole;setAge(I)V"))
    private boolean fusioncore$ageLockTick(Tadpole tadpole, int age) {
        return !this.fusioncore$ageLocked;
    }

    @Inject(method = "ageUp(I)V", cancellable = true, at = @At("HEAD"))
    private void fusioncore$ageLockFeed(int seconds, CallbackInfo ci) {
        if (this.fusioncore$ageLocked) {
            ci.cancel();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$saveAgeLock(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("AgeLocked", this.fusioncore$ageLocked);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$readAgeLock(CompoundTag tag, CallbackInfo ci) {
        this.fusioncore$ageLocked = tag.getBoolean("AgeLocked");
    }

    @Inject(method = "saveToBucketTag", at = @At("RETURN"))
    private void fusioncore$bucketAgeLock(ItemStack stack, CallbackInfo ci) {
        boolean locked = this.fusioncore$ageLocked;
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, stack, tag -> tag.putBoolean("AgeLocked", locked));
    }

    @Inject(method = "loadFromBucketTag", at = @At("RETURN"))
    private void fusioncore$unbucketAgeLock(CompoundTag tag, CallbackInfo ci) {
        this.fusioncore$ageLocked = tag.getBoolean("AgeLocked");
    }

    @Override
    public boolean fusioncore$ageLocked() {
        return this.fusioncore$ageLocked;
    }

    @Override
    public void fusioncore$setAgeLocked(boolean ageLocked) {
        this.fusioncore$ageLocked = ageLocked;
    }
}
