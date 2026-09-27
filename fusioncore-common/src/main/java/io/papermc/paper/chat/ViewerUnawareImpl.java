package io.papermc.paper.chat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/** Fusion Core: Paper API (1.21.1). Renders once and hands the same result to every viewer. */
sealed class ViewerUnawareImpl implements ChatRenderer, ChatRenderer.ViewerUnaware permits ViewerUnawareImpl.Default {

    private final ViewerUnaware unaware;
    private Component message;

    ViewerUnawareImpl(final ViewerUnaware unaware) {
        this.unaware = unaware;
    }

    @Override
    public Component render(final Player source, final Component sourceDisplayName, final Component message, final Audience viewer) {
        return this.render(source, sourceDisplayName, message);
    }

    @Override
    public Component render(final Player source, final Component sourceDisplayName, final Component message) {
        if (this.message == null) {
            this.message = this.unaware.render(source, sourceDisplayName, message);
        }
        return this.message;
    }

    static final class Default extends ViewerUnawareImpl implements ChatRenderer.Default {

        Default(final ViewerUnaware unaware) {
            super(unaware);
        }
    }
}
