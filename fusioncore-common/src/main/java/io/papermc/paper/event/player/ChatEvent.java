package io.papermc.paper.event.player;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.Warning;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Set;

/**
 * Fired on the main thread after {@link AsyncChatEvent}.
 *
 * @deprecated Listening to this event forces chat to wait for the main thread, delaying chat messages.
 * It is recommended to use {@link AsyncChatEvent} instead, wherever possible.
 */
@Deprecated
@Warning(reason = "Listening to this event forces chat to wait for the main thread, delaying chat messages.")
public final class ChatEvent extends AbstractChatEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public ChatEvent(final Player player, final Set<Audience> viewers, final ChatRenderer renderer, final Component message, final Component originalMessage, final SignedMessage signedMessage) {
        super(false, player, viewers, renderer, message, originalMessage, signedMessage);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
