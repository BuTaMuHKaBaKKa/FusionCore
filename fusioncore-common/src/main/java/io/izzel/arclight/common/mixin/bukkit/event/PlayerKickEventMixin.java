package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerKickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.bukkit.event.player.PlayerKickEvent$Cause;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's component kick reason and leave message, and the kick cause (Paper patch 0006 "Adventure"). Stored as the legacy string Spigot uses, so colours
 * and RGB survive and click/hover do not.
 */
@Mixin(value = PlayerKickEvent.class, remap = false)
public abstract class PlayerKickEventMixin {

    @Shadow public abstract String getReason();

    @Shadow public abstract void setReason(String value);

    @Shadow public abstract String getLeaveMessage();

    @Shadow public abstract void setLeaveMessage(String value);


    public Component reason() {
        return FusionCoreAdventure.fromLegacy(this.getReason());
    }

    public void reason(Component reason) {
        this.setReason(FusionCoreAdventure.toLegacyRgb(reason == null ? Component.empty() : reason));
    }

    public Component leaveMessage() {
        return FusionCoreAdventure.fromLegacy(this.getLeaveMessage());
    }

    public void leaveMessage(Component leaveMessage) {
        this.setLeaveMessage(FusionCoreAdventure.toLegacyRgb(leaveMessage == null ? Component.empty() : leaveMessage));
    }

    // Paper's kick cause: set by whoever kicks (see FusionCoreEvents), UNKNOWN when nobody says
    private PlayerKickEvent$Cause fusioncore$cause;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fusioncore$takeCause(CallbackInfo ci) {
        this.fusioncore$cause = io.izzel.arclight.common.mod.util.FusionCoreEvents.takeKickCause();
    }

    public PlayerKickEvent$Cause getCause() {
        return this.fusioncore$cause;
    }
}
