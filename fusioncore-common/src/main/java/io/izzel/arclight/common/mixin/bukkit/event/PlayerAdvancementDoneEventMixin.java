package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.PlayerAdvancementDoneEventBridge;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's advancement message; broadcast after the event, as Paper does. */
@Mixin(value = PlayerAdvancementDoneEvent.class, remap = false)
public class PlayerAdvancementDoneEventMixin implements PlayerAdvancementDoneEventBridge {

    @Unique private Component fusioncore$message;

    public Component message() {
        return this.fusioncore$message;
    }

    public void message(Component message) {
        this.fusioncore$message = message;
    }

    @Override
    public Component fusioncore$message() {
        return this.fusioncore$message;
    }

    @Override
    public void fusioncore$setMessage(Component message) {
        this.fusioncore$message = message;
    }
}
