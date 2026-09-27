package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Fusion Core: Paper's Adventure accessors for the quit message. Stored as the legacy string
 * Spigot broadcasts, so RGB survives and click/hover do not.
 */
@Mixin(value = PlayerQuitEvent.class, remap = false)
public abstract class PlayerQuitEventMixin {

    @Shadow public abstract String getQuitMessage();

    @Shadow public abstract void setQuitMessage(String quitMessage);

    // Paper's quit reason (Paper patch 0430): set by the core before it builds the event, DISCONNECTED otherwise
    private org.bukkit.event.player.PlayerQuitEvent$QuitReason fusioncore$reason;

    @org.spongepowered.asm.mixin.injection.Inject(method = "<init>", at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private void fusioncore$takeReason(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        this.fusioncore$reason = io.izzel.arclight.common.mod.util.FusionCoreEvents.takeQuitReason();
    }

    public org.bukkit.event.player.PlayerQuitEvent$QuitReason getReason() {
        return this.fusioncore$reason;
    }

    public Component quitMessage() {
        String legacy = this.getQuitMessage();
        return legacy == null ? null : FusionCoreAdventure.fromLegacy(legacy);
    }

    public void quitMessage(Component quitMessage) {
        this.setQuitMessage(quitMessage == null ? null : FusionCoreAdventure.toLegacyRgb(quitMessage));
    }
}
