package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.GameModeChangeEventBridge;
import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent$Cause;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's cause and cancel message on {@link PlayerGameModeChangeEvent}. */
@Mixin(value = PlayerGameModeChangeEvent.class, remap = false)
public abstract class PlayerGameModeChangeEventMixin implements GameModeChangeEventBridge {

    private PlayerGameModeChangeEvent$Cause fusioncore$cause;
    private Component fusioncore$cancelMessage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fusioncore$takeCause(CallbackInfo ci) {
        FusionCoreEvents.GameModeChange pending = FusionCoreEvents.takeGameModeChange();
        this.fusioncore$cause = pending.cause();
        this.fusioncore$cancelMessage = pending.cancelMessage();
    }

    public PlayerGameModeChangeEvent$Cause getCause() {
        return this.fusioncore$cause;
    }

    public Component cancelMessage() {
        return this.fusioncore$cancelMessage;
    }

    public void cancelMessage(Component message) {
        this.fusioncore$cancelMessage = message;
    }

    @Override
    public Component fusioncore$cancelMessage() {
        return this.fusioncore$cancelMessage;
    }
}
