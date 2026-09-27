package io.izzel.arclight.common.mixin.core.world.entity.monster;

import io.izzel.arclight.common.bridge.core.world.entity.monster.EnderManBridge;
import io.izzel.arclight.common.mixin.core.world.entity.PathfinderMobMixin;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderMan.class)
public abstract class EnderManMixin extends PathfinderMobMixin implements EnderManBridge {

    // @formatter:off
    @Shadow private int targetChangeTime;
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_CREEPY;
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_STARED_AT;
    @Shadow @Final private static AttributeModifier SPEED_MODIFIER_ATTACKING;
    @Shadow @Final private static ResourceLocation SPEED_MODIFIER_ATTACKING_ID;
    // @formatter:on

    @Override
    public void bridge$updateTarget(LivingEntity livingEntity) {
        AttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (livingEntity == null) {
            this.targetChangeTime = 0;
            this.entityData.set(DATA_CREEPY, false);
            this.entityData.set(DATA_STARED_AT, false);
            modifiableattributeinstance.removeModifier(SPEED_MODIFIER_ATTACKING);
        } else {
            this.targetChangeTime = this.tickCount;
            this.entityData.set(DATA_CREEPY, true);
            if (!modifiableattributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING_ID)) {
                modifiableattributeinstance.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }
        }
    }

    @Override
    public boolean setTarget(LivingEntity livingEntity, EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        if (!super.setTarget(livingEntity, reason, fireEvent)) {
            return false;
        }
        bridge$updateTarget(getTarget());
        return true;
    }

    @Inject(method = "setTarget", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/monster/Monster;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void arclight$returnIfFailed(LivingEntity livingEntity, CallbackInfo ci) {
        if (!arclight$targetSuccess) {
            ci.cancel();
        }
    }

    // Fusion Core: Paper's EndermanAttackPlayerEvent (Paper patch 0189): cancelled up front when vanilla would not attack
    @com.llamalad7.mixinextras.injector.ModifyReturnValue(method = "isLookingAtMe", at = @At("RETURN"))
    private boolean fusioncore$attackPlayerEvent(boolean shouldAttack, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.world.entity.player.Player player) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents.endermanAttack((EnderMan) (Object) this, player, shouldAttack);

    }

    // Fusion Core: Paper's EndermanEscapeEvent (patch 0184): RUNAWAY (sunlight), INDIRECT (projectiles and potions)
    @org.spongepowered.asm.mixin.Unique
    private boolean fusioncore$escapeRefused;
    @org.spongepowered.asm.mixin.Unique
    private Boolean fusioncore$indirectEscape;

    @com.llamalad7.mixinextras.injector.v2.WrapWithCondition(method = "customServerAiStep", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/monster/EnderMan;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private boolean fusioncore$runawayEscape(net.minecraft.world.entity.monster.EnderMan self, LivingEntity target) {
        this.fusioncore$escapeRefused = !io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.endermanEscape(self, "RUNAWAY");
        return !this.fusioncore$escapeRefused;
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "customServerAiStep", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/monster/EnderMan;teleport()Z"))
    private boolean fusioncore$runawayTeleport(net.minecraft.world.entity.monster.EnderMan self, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original) {
        if (this.fusioncore$escapeRefused) {
            this.fusioncore$escapeRefused = false;
            return false;
        }
        return original.call(self);
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void fusioncore$resetIndirectEscape(net.minecraft.world.damagesource.DamageSource source, float amount, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        this.fusioncore$indirectEscape = null;
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "hurt", at = @At(value = "INVOKE", ordinal = 1,
        target = "Lnet/minecraft/world/entity/monster/EnderMan;teleport()Z"))
    private boolean fusioncore$indirectEscape(net.minecraft.world.entity.monster.EnderMan self, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original) {
        if (this.fusioncore$indirectEscape == null) {
            this.fusioncore$indirectEscape = io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.endermanEscape(self, "INDIRECT");
        }
        return this.fusioncore$indirectEscape && original.call(self);
    }
}
