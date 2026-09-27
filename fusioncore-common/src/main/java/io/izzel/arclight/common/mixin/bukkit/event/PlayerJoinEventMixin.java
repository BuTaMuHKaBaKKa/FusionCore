package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Fusion Core: Paper's Adventure accessors for the join message. Stored as the legacy string
 * Spigot broadcasts, so RGB survives and click/hover do not.
 */
@Mixin(value = PlayerJoinEvent.class, remap = false)
public abstract class PlayerJoinEventMixin {

    @Shadow public abstract String getJoinMessage();

    @Shadow public abstract void setJoinMessage(String joinMessage);

    public Component joinMessage() {
        String legacy = this.getJoinMessage();
        return legacy == null ? null : FusionCoreAdventure.fromLegacy(legacy);
    }

    public void joinMessage(Component joinMessage) {
        this.setJoinMessage(joinMessage == null ? null : FusionCoreAdventure.toLegacyRgb(joinMessage));
    }
}
