package com.destroystokyo.paper.event.brigadier;

import com.mojang.brigadier.suggestion.Suggestions;
import io.izzel.arclight.common.mixin.bukkit.event.EventAccessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when sending {@link Suggestions} to the client. Will be called asynchronously if a plugin
 * marks the {@link com.destroystokyo.paper.event.server.AsyncTabCompleteEvent} event handled asynchronously,
 * otherwise called synchronously.
 */
public class AsyncPlayerSendSuggestionsEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled = false;

    private Suggestions suggestions;
    private final String buffer;

    @ApiStatus.Internal
    public AsyncPlayerSendSuggestionsEvent(final @NotNull Player player, final @NotNull Suggestions suggestions, final @NotNull String buffer) {
        super(player);
        // Fusion Core: PlayerEvent(Player, boolean) is package-private on Spigot
        ((EventAccessor) this).fusioncore$setAsync(!Bukkit.isPrimaryThread());
        this.suggestions = suggestions;
        this.buffer = buffer;
    }

    /**
     * Gets the input buffer sent to request these suggestions.
     *
     * @return the input buffer
     */
    public @NotNull String getBuffer() {
        return this.buffer;
    }

    /**
     * Gets the suggestions to be sent to client.
     *
     * @return the suggestions
     */
    public @NotNull Suggestions getSuggestions() {
        return this.suggestions;
    }

    /**
     * Sets the suggestions to be sent to client.
     *
     * @param suggestions suggestions
     */
    public void setSuggestions(final @NotNull Suggestions suggestions) {
        this.suggestions = suggestions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Cancels sending suggestions to the client.
     * {@inheritDoc}
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
