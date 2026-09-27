package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.event.server.ServerListPingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Fusion Core: Paper's component MOTD (Paper patch 0006 "Adventure"). Stored as the legacy string Spigot uses, so colours
 * and RGB survive and click/hover do not.
 */
@Mixin(value = ServerListPingEvent.class, remap = false)
public abstract class ServerListPingEventMixin {

    @Shadow public abstract String getMotd();

    @Shadow public abstract void setMotd(String value);


    public Component motd() {
        return FusionCoreAdventure.fromLegacy(this.getMotd());
    }

    public void motd(Component motd) {
        this.setMotd(FusionCoreAdventure.toLegacyRgb(motd == null ? Component.empty() : motd));
    }

    // Fusion Core: Paper's constructors for subclasses that count the players through iterator(): the MOTD component is
    // kept as legacy text, as above; public here, protected in Paper

    @org.spongepowered.asm.mixin.Mutable @org.spongepowered.asm.mixin.Final @Shadow private String hostname;
    @org.spongepowered.asm.mixin.Mutable @org.spongepowered.asm.mixin.Final @Shadow private java.net.InetAddress address;
    @Shadow private String motd;
    @org.spongepowered.asm.mixin.Mutable @org.spongepowered.asm.mixin.Final @Shadow private int numPlayers;
    @Shadow private int maxPlayers;

    @io.izzel.arclight.common.mod.mixins.annotation.ShadowConstructor.Super
    public void fusioncore$constructor$super(boolean isAsync) {
        throw new AbstractMethodError();
    }

    @io.izzel.arclight.common.mod.mixins.annotation.CreateConstructor
    public void fusioncore$constructor(String hostname, java.net.InetAddress address, Component motd, int maxPlayers) {
        fusioncore$constructor$super(true);
        this.numPlayers = Integer.MIN_VALUE; // Spigot's MAGIC_PLAYER_COUNT
        this.hostname = hostname;
        this.address = address;
        this.motd = FusionCoreAdventure.toLegacyRgb(motd == null ? Component.empty() : motd);
        this.maxPlayers = maxPlayers;
    }

    @io.izzel.arclight.common.mod.mixins.annotation.CreateConstructor
    public void fusioncore$constructor(java.net.InetAddress address, Component motd, int maxPlayers) {
        fusioncore$constructor$super(true);
        this.numPlayers = Integer.MIN_VALUE;
        this.hostname = "";
        this.address = address;
        this.motd = FusionCoreAdventure.toLegacyRgb(motd == null ? Component.empty() : motd);
        this.maxPlayers = maxPlayers;
    }
}
