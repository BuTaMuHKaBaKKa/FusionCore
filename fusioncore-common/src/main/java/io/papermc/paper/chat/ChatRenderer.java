package io.papermc.paper.chat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * Fusion Core: Paper API (1.21.1), same signatures as Paper. Renders chat messages sent by
 * {@link Player}s; used by {@link io.papermc.paper.event.player.AsyncChatEvent}.
 */
@FunctionalInterface
public interface ChatRenderer {

    /**
     * Renders a chat message. This will be called once for each receiving {@link Audience}.
     *
     * @param source the message source
     * @param sourceDisplayName the display name of the source player
     * @param message the chat message
     * @param viewer the receiving {@link Audience}
     * @return a rendered chat message
     */
    Component render(Player source, Component sourceDisplayName, Component message, Audience viewer);

    /**
     * Create a new instance of the default {@link ChatRenderer}.
     *
     * @return a new {@link ChatRenderer}
     */
    static ChatRenderer defaultRenderer() {
        return new ViewerUnawareImpl.Default((source, sourceDisplayName, message) -> Component.translatable("chat.type.text", sourceDisplayName, message));
    }

    sealed interface Default extends ChatRenderer, ViewerUnaware permits ViewerUnawareImpl.Default {
    }

    /**
     * Creates a new viewer-unaware {@link ChatRenderer}, which will render the chat message a single time,
     * displaying the same rendered message to every viewing {@link Audience}.
     *
     * @param renderer the viewer unaware renderer
     * @return a new {@link ChatRenderer}
     */
    static ChatRenderer viewerUnaware(final ViewerUnaware renderer) {
        return new ViewerUnawareImpl(renderer);
    }

    /**
     * Similar to {@link ChatRenderer}, but without knowledge of the message viewer.
     *
     * @see ChatRenderer#viewerUnaware(ViewerUnaware)
     */
    interface ViewerUnaware {

        /**
         * Renders a chat message.
         *
         * @param source the message source
         * @param sourceDisplayName the display name of the source player
         * @param message the chat message
         * @return a rendered chat message
         */
        Component render(Player source, Component sourceDisplayName, Component message);
    }
}
