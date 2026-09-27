package io.izzel.arclight.common.mixin.core.world.entity.animal;

import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge;
import io.izzel.arclight.common.bridge.core.world.entity.MobBridge;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Pufferfish.class)
public abstract class PufferfishMixin {

    @Inject(method = "touch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private void arclight$attack(Mob mobEntity, CallbackInfo ci) {
        ((MobBridge) mobEntity).bridge$pushEffectCause(EntityPotionEffectEvent.Cause.ATTACK);
    }

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private void arclight$collide(Player entityIn, CallbackInfo ci) {
        ((LivingEntityBridge) entityIn).bridge$pushEffectCause(EntityPotionEffectEvent.Cause.ATTACK);
    }

    // Fusion Core: Paper's PufferFishStateChangeEvent (patch 0544): cancelled, no sound, no new state and the timer does not advance
    @org.spongepowered.asm.mixin.Unique
    private boolean fusioncore$puffRefused;

    @Inject(method = "tick", at = @At("HEAD"))
    private void fusioncore$resetPuff(CallbackInfo ci) {
        this.fusioncore$puffRefused = false;
    }

    @com.llamalad7.mixinextras.injector.v2.WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/animal/Pufferfish;makeSound(Lnet/minecraft/sounds/SoundEvent;)V"))
    private boolean fusioncore$puffState(Pufferfish fish, net.minecraft.sounds.SoundEvent sound) {
        int state = fish.getPuffState();
        int next = sound == net.minecraft.sounds.SoundEvents.PUFFER_FISH_BLOW_UP ? state + 1 : state - 1;
        this.fusioncore$puffRefused = !io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.pufferFishState(fish, next);
        return !this.fusioncore$puffRefused;
    }

    @com.llamalad7.mixinextras.injector.v2.WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/animal/Pufferfish;setPuffState(I)V"))
    private boolean fusioncore$puffStateSet(Pufferfish fish, int state) {
        return !this.fusioncore$puffRefused;
    }

    @com.llamalad7.mixinextras.injector.v2.WrapWithCondition(method = "tick", at = @At(value = "FIELD", opcode = org.objectweb.asm.Opcodes.PUTFIELD,
        target = "Lnet/minecraft/world/entity/animal/Pufferfish;inflateCounter:I"))
    private boolean fusioncore$puffInflate(Pufferfish fish, int value) {
        return !this.fusioncore$puffRefused;
    }

    @com.llamalad7.mixinextras.injector.v2.WrapWithCondition(method = "tick", at = @At(value = "FIELD", opcode = org.objectweb.asm.Opcodes.PUTFIELD,
        target = "Lnet/minecraft/world/entity/animal/Pufferfish;deflateTimer:I"))
    private boolean fusioncore$puffDeflate(Pufferfish fish, int value) {
        return !this.fusioncore$puffRefused;
    }
}
