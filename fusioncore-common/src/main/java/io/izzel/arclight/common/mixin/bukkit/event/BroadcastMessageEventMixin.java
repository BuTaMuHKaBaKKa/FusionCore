package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Fusion Core: Paper's component broadcast message (Paper patch 0006 "Adventure"). Stored as the legacy string Spigot uses, so colours
 * and RGB survive and click/hover do not.
 */
@Mixin(value = BroadcastMessageEvent.class, remap = false)
public abstract class BroadcastMessageEventMixin {

    @Shadow public abstract String getMessage();

    @Shadow public abstract void setMessage(String value);


    public Component message() {
        return FusionCoreAdventure.fromLegacy(this.getMessage());
    }

    public void message(Component message) {
        this.setMessage(FusionCoreAdventure.toLegacyRgb(message == null ? Component.empty() : message));
    }
}
