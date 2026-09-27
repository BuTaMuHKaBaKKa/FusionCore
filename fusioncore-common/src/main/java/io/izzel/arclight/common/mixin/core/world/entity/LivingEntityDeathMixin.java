package io.izzel.arclight.common.mixin.core.world.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.bukkit.EntityDeathEventBridge;
import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityDeathBridge;
import io.izzel.arclight.common.mod.util.FusionCoreDeathEvents;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Fusion Core: Paper's cancellable death (patch 0242). A cancelled death leaves the entity alive with the event's health.
 * NeoForge's {@code LivingDeathEvent} still runs before the event.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDeathMixin implements LivingEntityDeathBridge {

    @Shadow protected boolean dead;
    @Shadow public abstract void setHealth(float health);

    @Unique private boolean fusioncore$silentDeath;
    @Unique private boolean fusioncore$deathSoundRequested;
    @Unique private EntityDeathEvent fusioncore$deathEvent;
    @Unique private List<Runnable> fusioncore$deferredClears;
    @Unique private Runnable fusioncore$pendingKillScore;
    @Unique private Runnable fusioncore$pendingDeathGameEvent;

    @Override
    public boolean fusioncore$isSilentDeath() {
        return this.fusioncore$silentDeath;
    }

    @Override
    public void fusioncore$setDeathEvent(EntityDeathEvent event) {
        this.fusioncore$deathEvent = event;
    }

    @Override
    public @Nullable EntityDeathEvent fusioncore$takeDeathEvent() {
        EntityDeathEvent event = this.fusioncore$deathEvent;
        this.fusioncore$deathEvent = null;
        return event;
    }

    @Override
    public boolean fusioncore$deferDeathClear(Runnable clear) {
        if (this.fusioncore$deferredClears == null) {
            return false;
        }
        this.fusioncore$deferredClears.add(clear);
        return true;
    }

    // Paper: the death sound is played from the event; hurt only says whether it would have played (silentDeath)
    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;makeSound(Lnet/minecraft/sounds/SoundEvent;)V"))
    private void fusioncore$deathSoundFromEvent(LivingEntity instance, SoundEvent sound, Operation<Void> original) {
        this.fusioncore$deathSoundRequested = true;
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void fusioncore$silentDeath(LivingEntity instance, DamageSource source, Operation<Void> original) {
        this.fusioncore$silentDeath = !this.fusioncore$deathSoundRequested;
        this.fusioncore$deathSoundRequested = false;
        try {
            original.call(instance, source);
        } finally {
            this.fusioncore$silentDeath = false;
        }
    }

    @WrapOperation(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;awardKillScore(Lnet/minecraft/world/entity/Entity;ILnet/minecraft/world/damagesource/DamageSource;)V"))
    private void fusioncore$awardAfterEvent(LivingEntity killer, Entity victim, int score, DamageSource source, Operation<Void> original) {
        this.fusioncore$pendingKillScore = () -> original.call(killer, victim, score, source);
    }

    @WrapOperation(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/core/Holder;)V"))
    private void fusioncore$gameEventAfterEvent(LivingEntity instance, Holder<GameEvent> gameEvent, Operation<Void> original) {
        this.fusioncore$pendingDeathGameEvent = () -> original.call(instance, gameEvent);
    }

    @Inject(method = "die", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;createWitherRose(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void fusioncore$cancelledDeath(DamageSource source, CallbackInfo ci) {
        EntityDeathEvent event = this.fusioncore$takeDeathEvent();
        if (FusionCoreDeathEvents.isCancelled(event)) {
            this.fusioncore$pendingKillScore = null;
            this.fusioncore$pendingDeathGameEvent = null;
            this.dead = false;
            this.setHealth((float) ((EntityDeathEventBridge) event).bridge$getReviveHealth());
            ci.cancel();
            return;
        }
        this.fusioncore$runPendingDeath();
    }

    @Inject(method = "die", at = @At("RETURN"))
    private void fusioncore$diedWithoutEvent(DamageSource source, CallbackInfo ci) {
        this.fusioncore$runPendingDeath();
    }

    @Unique
    private void fusioncore$runPendingDeath() {
        Runnable killScore = this.fusioncore$pendingKillScore;
        Runnable gameEvent = this.fusioncore$pendingDeathGameEvent;
        this.fusioncore$pendingKillScore = null;
        this.fusioncore$pendingDeathGameEvent = null;
        if (killScore != null) {
            killScore.run();
        }
        if (gameEvent != null) {
            gameEvent.run();
        }
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void fusioncore$startDeathLoot(ServerLevel level, DamageSource source, CallbackInfo ci) {
        this.fusioncore$deathEvent = null;
        this.fusioncore$deferredClears = new ArrayList<>();
    }

    @Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
    private void fusioncore$finishDeathLoot(ServerLevel level, DamageSource source, CallbackInfo ci) {
        List<Runnable> clears = this.fusioncore$deferredClears;
        this.fusioncore$deferredClears = null;
        if (clears != null && !FusionCoreDeathEvents.isCancelled(this.fusioncore$deathEvent)) {
            clears.forEach(Runnable::run);
        }
    }
}
