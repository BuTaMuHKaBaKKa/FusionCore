package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Fusion Core: Paper's component kick message (Paper patch 0006 "Adventure"). Stored as the legacy string Spigot uses, so colours
 * and RGB survive and click/hover do not.
 */
@Mixin(value = PlayerPreLoginEvent.class, remap = false)
public abstract class PlayerPreLoginEventMixin {

    @Shadow public abstract String getKickMessage();

    @Shadow public abstract void setKickMessage(String value);


    public Component kickMessage() {
        return FusionCoreAdventure.fromLegacy(this.getKickMessage());
    }

    public void kickMessage(Component kickMessage) {
        this.setKickMessage(FusionCoreAdventure.toLegacyRgb(kickMessage == null ? Component.empty() : kickMessage));
    }

    @Shadow public abstract void disallow(PlayerPreLoginEvent.Result result, String message);

    public void disallow(PlayerPreLoginEvent.Result result, Component message) {
        this.disallow(result, FusionCoreAdventure.toLegacyRgb(message == null ? Component.empty() : message));
    }
}
