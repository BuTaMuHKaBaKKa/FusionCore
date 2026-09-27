package io.izzel.arclight.common.mixin.core.world.level.block.entity;

import io.izzel.arclight.common.bridge.core.world.level.block.entity.BeaconBlockEntityBridge;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.bukkit.craftbukkit.v.potion.CraftPotionUtil;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin implements BeaconBlockEntityBridge {

    // @formatter:off
    @Shadow public int levels;
    @Shadow @Nullable public Holder<MobEffect> primaryPower;
    @Shadow @Nullable public Holder<MobEffect> secondaryPower;
    // @formatter:on

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    public void arclight$level(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        this.levels = compoundTag.getInt("Levels");
    }

    public PotionEffect getPrimaryEffect() {
        return (this.primaryPower != null) ? CraftPotionUtil.toBukkit(new MobEffectInstance(this.primaryPower, this.getLevel(), this.getAmplification(), true, true)) : null;
    }

    public PotionEffect getSecondaryEffect() {
        return (this.hasSecondaryEffect()) ? CraftPotionUtil.toBukkit(new MobEffectInstance(this.secondaryPower, getLevel(), getAmplification(), true, true)) : null;
    }

    private byte getAmplification() {
        byte b0 = 0;
        if (this.levels >= 4 && this.primaryPower == this.secondaryPower) {
            b0 = 1;
        }
        return b0;
    }

    private int getLevel() {
        int i = (9 + this.levels * 2) * 20;
        return i;
    }

    private boolean hasSecondaryEffect() {
        if (this.levels >= 4 && this.primaryPower != this.secondaryPower && this.secondaryPower != null) {
            return true;
        }
        return false;
    }

    @Override
    public PotionEffect bridge$getPrimaryEffect() {
        return getPrimaryEffect();
    }

    @Override
    public PotionEffect bridge$getSecondaryEffect() {
        return getSecondaryEffect();
    }

    // Fusion Core: Paper's BeaconActivatedEvent / BeaconDeactivatedEvent (Paper patch 0520) when the pyramid check changes
    // the beacon's level to or from 0, and BeaconDeactivatedEvent when the beacon is removed (vanilla's deactivate sound)

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;updateBase(Lnet/minecraft/world/level/Level;III)I"))
    private static int fusioncore$beaconLevels(net.minecraft.world.level.Level level, int x, int y, int z, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Integer> original,
                                             @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.core.BlockPos pos, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.world.level.block.entity.BeaconBlockEntity beacon) {
        int before = beacon.levels;
        int after = original.call(level, x, y, z);
        if (!level.isClientSide) {
            if (before <= 0 && after > 0) {
                io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.beaconActivated(level, pos);
            } else if (before > 0 && after <= 0) {
                io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.beaconDeactivated(level, pos);
            }
        }
        return after;
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "setRemoved", at = @At("HEAD"))
    private void fusioncore$beaconRemoved(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        net.minecraft.world.level.block.entity.BeaconBlockEntity self = (net.minecraft.world.level.block.entity.BeaconBlockEntity) (Object) this;
        if (self.getLevel() != null && !self.getLevel().isClientSide) {
            io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.beaconDeactivated(self.getLevel(), self.getBlockPos());
        }
    }

    // Fusion Core: Paper's BeaconEffectEvent (Paper patch 0049), per player and effect; the effect also gets Spigot's
    // EntityPotionEffectEvent cause BEACON, which Arclight did not set (the event said UNKNOWN)

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "applyEffects", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private static boolean fusioncore$beaconPrimary(net.minecraft.world.entity.player.Player player, MobEffectInstance effect, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original,
                                                  @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.world.level.Level level, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.core.BlockPos pos) {
        MobEffectInstance given = io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.beaconEffect(player, effect, level, pos, true);
        if (given == null) {
            return false;
        }
        ((io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge) player).bridge$pushEffectCause(org.bukkit.event.entity.EntityPotionEffectEvent.Cause.BEACON);
        return original.call(player, given);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "applyEffects", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private static boolean fusioncore$beaconSecondary(net.minecraft.world.entity.player.Player player, MobEffectInstance effect, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original,
                                                    @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.world.level.Level level, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.core.BlockPos pos) {
        MobEffectInstance given = io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.beaconEffect(player, effect, level, pos, false);
        if (given == null) {
            return false;
        }
        ((io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge) player).bridge$pushEffectCause(org.bukkit.event.entity.EntityPotionEffectEvent.Cause.BEACON);
        return original.call(player, given);
    }

    // Fusion Core: Paper patch 0429 "Beacon API - custom effect ranges": the range is saved as Paper.Range (-1 = vanilla)
    // and used by the effect sweep of this beacon and by CraftBeacon#getEntitiesInRange (CraftBeaconMixin)

    @org.spongepowered.asm.mixin.Unique private double fusioncore$effectRange = -1;
    @org.spongepowered.asm.mixin.Unique private static final ThreadLocal<BeaconBlockEntity> FUSIONCORE_TICKING = new ThreadLocal<>();

    @Override
    public double fusioncore$effectRange() {
        return this.fusioncore$effectRange < 0 ? this.levels * 10 + 10 : this.fusioncore$effectRange;
    }

    @Override
    public void fusioncore$setEffectRange(double range) {
        this.fusioncore$effectRange = range;
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void fusioncore$loadRange(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        this.fusioncore$effectRange = tag.contains("Paper.Range", net.minecraft.nbt.Tag.TAG_DOUBLE) ? tag.getDouble("Paper.Range") : -1;
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void fusioncore$saveRange(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        tag.putDouble("Paper.Range", this.fusioncore$effectRange);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;applyEffects(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)V"))
    private static void fusioncore$tickingBeacon(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, int levels, Holder<MobEffect> primary, Holder<MobEffect> secondary,
                                               com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
                                               @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) BeaconBlockEntity beacon) {
        FUSIONCORE_TICKING.set(beacon);
        try {
            original.call(level, pos, levels, primary, secondary);
        } finally {
            FUSIONCORE_TICKING.remove();
        }
    }

    @org.spongepowered.asm.mixin.injection.ModifyArg(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"))
    private static double fusioncore$effectRange(double range) {
        BeaconBlockEntity beacon = FUSIONCORE_TICKING.get();
        return beacon == null ? range : ((BeaconBlockEntityBridge) beacon).fusioncore$effectRange();
    }

    // Fusion Core: Paper's BlockLockCheckEvent (patch 0778)
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "createMenu", at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/entity/BaseContainerBlockEntity;canUnlock(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/LockCode;Lnet/minecraft/network/chat/Component;)Z"))
    private boolean fusioncore$lockCheck(net.minecraft.world.entity.player.Player player, net.minecraft.world.LockCode lock, net.minecraft.network.chat.Component name,
                                       com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original) {
        Boolean result = io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.lockCheck(player, lock, name, (net.minecraft.world.level.block.entity.BlockEntity) (Object) this);
        return result != null ? result : original.call(player, lock, name);
    }
}
