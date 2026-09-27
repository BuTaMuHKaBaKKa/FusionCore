// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package com.destroystokyo.paper.event.player;

import org.bukkit.Location;
import org.bukkit.block.EndGateway;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a teleport is triggered for an End Gateway
 */
public class PlayerTeleportEndGatewayEvent extends PlayerTeleportEvent {

    private final EndGateway gateway;

    @ApiStatus.Internal
    public PlayerTeleportEndGatewayEvent(final Player player, final Location from, final Location to, final EndGateway gateway) {
        super(player, from, to, PlayerTeleportEvent.TeleportCause.END_GATEWAY);
        this.gateway = gateway;
    }

    /**
     * The gateway triggering the teleport
     *
     * @return EndGateway used
     */
    public EndGateway getGateway() {
        return this.gateway;
    }
}
