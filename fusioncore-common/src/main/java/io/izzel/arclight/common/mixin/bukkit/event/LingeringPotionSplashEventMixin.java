package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.LingeringPotionSplashEventBridge;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Fusion Core: Paper's {@code allowsEmptyCreation} (Paper patch 0425 "Expand LingeringPotion API"): when a listener clears
 * the cloud's effects, the cloud is not spawned unless this is set. Read by {@code ThrownPotionMixin}.
 */
@Mixin(value = LingeringPotionSplashEvent.class, remap = false)
public abstract class LingeringPotionSplashEventMixin implements LingeringPotionSplashEventBridge {

    @Unique private boolean fusioncore$allowEmptyAreaEffectCreation;

    public void allowsEmptyCreation(boolean allowEmptyAreaEffectCreation) {
        this.fusioncore$allowEmptyAreaEffectCreation = allowEmptyAreaEffectCreation;
    }

    @Override
    public boolean allowsEmptyCreation() {
        return this.fusioncore$allowEmptyAreaEffectCreation;
    }
}
