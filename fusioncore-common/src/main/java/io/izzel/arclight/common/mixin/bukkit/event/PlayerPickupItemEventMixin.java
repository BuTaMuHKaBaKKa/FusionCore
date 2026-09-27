package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.PlayerEventExtrasBridge;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's {@code PlayerPickupItemEvent#setFlyAtPlayer} (API patch 0058, server 0133): whether the item flies to the
 * player (the pickup animation). Cancelling turns it off and uncancelling on, as in Paper.
 */
@Mixin(value = PlayerPickupItemEvent.class, remap = false)
public abstract class PlayerPickupItemEventMixin implements PlayerEventExtrasBridge.Pickup {

    @Unique private boolean fusioncore$flyAtPlayer = true;

    public void setFlyAtPlayer(boolean flyAtPlayer) {
        this.fusioncore$flyAtPlayer = flyAtPlayer;
    }

    public boolean getFlyAtPlayer() {
        return this.fusioncore$flyAtPlayer;
    }

    @Inject(method = "setCancelled", at = @At("HEAD"))
    private void fusioncore$flyUnlessCancelled(boolean cancel, CallbackInfo ci) {
        this.fusioncore$flyAtPlayer = !cancel;
    }

    @Override
    public boolean bridge$getFlyAtPlayer() {
        return this.fusioncore$flyAtPlayer;
    }
}
