package io.papermc.paper.event.player;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Set;

/**
 * Fired when a {@link Player} sends a chat message to the server.
 * <p>
 * This event will sometimes fire synchronously, depending on how it was
 * triggered.
 * <p>
 * If a player is the direct cause of this event by an incoming packet, this
 * event will be asynchronous. If a plugin triggers this event by compelling a
 * player to chat, this event will be synchronous.
 * <p>
 * Care should be taken to check {@link #isAsynchronous()} and treat the event
 * appropriately.
 */
public final class AsyncChatEvent extends AbstractChatEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public AsyncChatEvent(final boolean async, final Player player, final Set<Audience> viewers, final ChatRenderer renderer, final Component message, final Component originalMessage, final SignedMessage signedMessage) {
        super(async, player, viewers, renderer, message, originalMessage, signedMessage);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
