package io.izzel.arclight.common.mixin.bukkit;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.izzel.arclight.common.bridge.bukkit.PotionEffectBridge;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/** Fusion Core: Paper's {@code PotionEffect} additions: {@code with*} copies and the hidden effect. */
@Mixin(value = PotionEffect.class, remap = false)
public abstract class PotionEffectMixin implements PotionEffectBridge {

    // @formatter:off
    @Shadow @Final private int amplifier;
    @Shadow @Final private int duration;
    @Shadow @Final private PotionEffectType type;
    @Shadow @Final private boolean ambient;
    @Shadow @Final private boolean particles;
    @Shadow @Final private boolean icon;
    // @formatter:on

    @Unique
    private static final String fusioncore$HIDDEN_EFFECT = "hidden_effect";

    @Unique
    private PotionEffect fusioncore$hiddenEffect;

    @Override
    public void bridge$setHiddenEffect(PotionEffect hiddenEffect) {
        this.fusioncore$hiddenEffect = hiddenEffect;
    }

    public PotionEffect getHiddenPotionEffect() {
        return this.fusioncore$hiddenEffect;
    }

    public PotionEffect withType(PotionEffectType type) {
        return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
    }

    public PotionEffect withDuration(int duration) {
        return new PotionEffect(this.type, duration, amplifier, ambient, particles, icon);
    }

    public PotionEffect withAmplifier(int amplifier) {
        return new PotionEffect(this.type, duration, amplifier, ambient, particles, icon);
    }

    public PotionEffect withAmbient(boolean ambient) {
        return new PotionEffect(this.type, duration, amplifier, ambient, particles, icon);
    }

    public PotionEffect withParticles(boolean particles) {
        return new PotionEffect(this.type, duration, amplifier, ambient, particles, icon);
    }

    public PotionEffect withIcon(boolean icon) {
        return new PotionEffect(this.type, duration, amplifier, ambient, particles, icon);
    }

    @Inject(method = "<init>(Ljava/util/Map;)V", at = @At("RETURN"))
    private void fusioncore$deserializeHidden(Map<String, Object> map, CallbackInfo ci) {
        this.fusioncore$hiddenEffect = (PotionEffect) map.get(fusioncore$HIDDEN_EFFECT);
    }

    @ModifyReturnValue(method = "serialize", at = @At("RETURN"))
    private Map<String, Object> fusioncore$serializeHidden(Map<String, Object> map) {
        if (this.fusioncore$hiddenEffect == null) {
            return map;
        }
        return com.google.common.collect.ImmutableMap.<String, Object>builder().putAll(map).put(fusioncore$HIDDEN_EFFECT, this.fusioncore$hiddenEffect).build();
    }

    @ModifyReturnValue(method = "equals", at = @At("RETURN"))
    private boolean fusioncore$equalsHidden(boolean equal, Object obj) {
        return equal && java.util.Objects.equals(this.fusioncore$hiddenEffect, ((PotionEffectMixin) obj).fusioncore$hiddenEffect);
    }

    @ModifyReturnValue(method = "hashCode", at = @At("RETURN"))
    private int fusioncore$hashHidden(int hash) {
        return this.fusioncore$hiddenEffect == null ? hash : hash * 31 + this.fusioncore$hiddenEffect.hashCode();
    }
}
