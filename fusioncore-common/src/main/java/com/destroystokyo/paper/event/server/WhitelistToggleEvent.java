// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package com.destroystokyo.paper.event.server;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event is fired when whitelist is toggled
 *
 * @author Mark Vainomaa
 */
public class WhitelistToggleEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final boolean enabled;

    @ApiStatus.Internal
    public WhitelistToggleEvent(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets whether whitelist is going to be enabled or not
     *
     * @return Whether whitelist is going to be enabled or not
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
