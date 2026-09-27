package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCorePlayers;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper keeps the old {@code Player.Spigot#getPing} (deprecated for {@code Player#getPing}); Spigot dropped it. */
@Mixin(value = Player.Spigot.class, remap = false)
public abstract class PlayerSpigotMixin {

    @Deprecated
    public int getPing() {
        return FusionCorePlayers.spigotPing(this);
    }
}
