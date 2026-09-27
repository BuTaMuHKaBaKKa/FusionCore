package io.izzel.arclight.common.mixin.core.world.entity.decoration;

import io.izzel.arclight.common.bridge.core.world.entity.ArmorStandPaperBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's armor stand ticking switch (patch 0217, saved as {@code Paper.CanTickOverride}) and movement
 * switch (patch 0118).
 */
@Mixin(ArmorStand.class)
public abstract class ArmorStandPaperMixin implements ArmorStandPaperBridge {

    // @formatter:off
    @Shadow private boolean isDisabled(EquipmentSlot slot) { throw new AssertionError(); }
    // @formatter:on

    @Unique private boolean fusioncore$canTick = true;
    @Unique private boolean fusioncore$canTickSetByApi;
    @Unique private boolean fusioncore$equipmentDirty;
    @Unique private boolean fusioncore$canMove = true;

    @Override
    public boolean fusioncore$canMove() {
        return this.fusioncore$canMove;
    }

    @Override
    public void fusioncore$setCanMove(boolean canMove) {
        this.fusioncore$canMove = canMove;
    }

    @Override
    public boolean fusioncore$canTick() {
        return this.fusioncore$canTick;
    }

    @Override
    public void fusioncore$setCanTick(boolean canTick) {
        this.fusioncore$canTick = canTick;
        this.fusioncore$canTickSetByApi = true;
    }

    @Override
    public boolean fusioncore$isDisabled(EquipmentSlot slot) {
        return this.isDisabled(slot);
    }

    @Inject(method = "tick", cancellable = true, at = @At("HEAD"))
    private void fusioncore$canTick(CallbackInfo ci) {
        if (this.fusioncore$canTick) {
            return;
        }
        ArmorStand self = (ArmorStand) (Object) this;
        // the pose part of ArmorStand#tick, as Paper's updatePose()
        net.minecraft.network.syncher.SynchedEntityData data = self.getEntityData();
        if (!self.headPose.equals(data.get(ArmorStand.DATA_HEAD_POSE))) self.setHeadPose(data.get(ArmorStand.DATA_HEAD_POSE));
        if (!self.bodyPose.equals(data.get(ArmorStand.DATA_BODY_POSE))) self.setBodyPose(data.get(ArmorStand.DATA_BODY_POSE));
        if (!self.leftArmPose.equals(data.get(ArmorStand.DATA_LEFT_ARM_POSE))) self.setLeftArmPose(data.get(ArmorStand.DATA_LEFT_ARM_POSE));
        if (!self.rightArmPose.equals(data.get(ArmorStand.DATA_RIGHT_ARM_POSE))) self.setRightArmPose(data.get(ArmorStand.DATA_RIGHT_ARM_POSE));
        if (!self.leftLegPose.equals(data.get(ArmorStand.DATA_LEFT_LEG_POSE))) self.setLeftLegPose(data.get(ArmorStand.DATA_LEFT_LEG_POSE));
        if (!self.rightLegPose.equals(data.get(ArmorStand.DATA_RIGHT_LEG_POSE))) self.setRightLegPose(data.get(ArmorStand.DATA_RIGHT_LEG_POSE));
        if (this.fusioncore$equipmentDirty) {
            this.fusioncore$equipmentDirty = false;
            ((LivingEntity) self).detectEquipmentUpdates();
        }
        ci.cancel();
    }

    @Inject(method = "setItemSlot", at = @At("RETURN"))
    private void fusioncore$equipmentDirty(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        this.fusioncore$equipmentDirty = true;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$saveCanTick(CompoundTag tag, CallbackInfo ci) {
        if (this.fusioncore$canTickSetByApi) {
            tag.putBoolean("Paper.CanTickOverride", this.fusioncore$canTick);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$readCanTick(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("Paper.CanTickOverride")) {
            this.fusioncore$canTick = tag.getBoolean("Paper.CanTickOverride");
            this.fusioncore$canTickSetByApi = true;
        }
    }
}
