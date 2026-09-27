/*
 * Copyright (c) 2017 Daniel Ennis (Aikar) MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.destroystokyo.paper.event.server;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import net.kyori.examination.string.StringExaminer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.stream.Stream;

/**
 * Allows plugins to compute tab completion results asynchronously.
 * <p>
 * If this event provides completions, then the standard synchronous process
 * will not be fired to populate the results.
 * However, the synchronous TabCompleteEvent will fire with the Async results.
 * <p>
 * Only 1 process will be allowed to provide completions, the Async Event, or the standard process.
 */
public class AsyncTabCompleteEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CommandSender sender;
    private final String buffer;
    private final boolean isCommand;
    private final @Nullable Location location;
    private final List<Completion> completions = new ArrayList<>();
    private final List<String> stringCompletions = new StringView();
    private boolean handled;
    private boolean cancelled;

    public AsyncTabCompleteEvent(final @NotNull CommandSender sender, final @NotNull String buffer, final boolean isCommand, final @Nullable Location loc) {
        super(true);
        this.sender = sender;
        this.buffer = buffer;
        this.isCommand = isCommand;
        this.location = loc;
    }

    @Deprecated
    public AsyncTabCompleteEvent(final @NotNull CommandSender sender, final @NotNull List<String> completions, final @NotNull String buffer, final boolean isCommand, final @Nullable Location loc) {
        super(true);
        this.sender = sender;
        this.completions.addAll(fromStrings(completions));
        this.buffer = buffer;
        this.isCommand = isCommand;
        this.location = loc;
    }

    /**
     * Get the sender completing this command.
     *
     * @return the {@link CommandSender} instance
     */
    public @NotNull CommandSender getSender() {
        return this.sender;
    }

    /**
     * The list of completions which will be offered to the sender, in order.
     * This list is mutable and reflects what will be offered.
     *
     * @return a list of offered completions
     */
    public @NotNull List<String> getCompletions() {
        return this.stringCompletions;
    }

    /**
     * Set the completions offered, overriding any already set.
     * The passed collection will be cloned to a new {@code List}.
     *
     * @param completions the new completions
     */
    public void setCompletions(final @NotNull List<String> completions) {
        Preconditions.checkArgument(completions != null, "Completions list cannot be null");
        if (completions == this.stringCompletions) {
            return;
        }
        this.completions.clear();
        this.completions.addAll(fromStrings(completions));
    }

    /**
     * The list of {@link Completion completions} which will be offered to the sender, in order.
     * This list is mutable and reflects what will be offered.
     *
     * @return a list of offered completions
     */
    public @NotNull List<Completion> completions() {
        return this.completions;
    }

    /**
     * Set the {@link Completion completions} offered, overriding any already set.
     * The passed collection will be cloned to a new {@code List}.
     *
     * @param newCompletions the new completions
     */
    public void completions(final @NotNull List<Completion> newCompletions) {
        Preconditions.checkArgument(newCompletions != null, "new completions cannot be null");
        this.completions.clear();
        this.completions.addAll(newCompletions);
    }

    /**
     * @return command buffer, as entered
     */
    public @NotNull String getBuffer() {
        return this.buffer;
    }

    /**
     * @return {@code true} if it is a command being tab completed, {@code false} if it is a chat message.
     */
    public boolean isCommand() {
        return this.isCommand;
    }

    /**
     * @return The position looked at by the sender, or {@code null} if none
     */
    public @Nullable Location getLocation() {
        return this.location != null ? this.location.clone() : null;
    }

    /**
     * @return Is completions considered handled. Always {@code true} if completions is not empty.
     */
    public boolean isHandled() {
        return !this.completions.isEmpty() || this.handled;
    }

    /**
     * Sets whether to consider the completion request handled.
     *
     * @param handled if this completion should be marked as being handled
     */
    public void setHandled(final boolean handled) {
        this.handled = handled;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Will provide no completions, and will not fire the synchronous process
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

    private static List<Completion> fromStrings(final List<String> suggestions) {
        final List<Completion> list = new ArrayList<>(suggestions.size());
        for (final String suggestion : suggestions) {
            list.add(new CompletionImpl(suggestion, null));
        }
        return list;
    }

    // Paper uses io.papermc.paper.util.TransformingRandomAccessList for this view
    private final class StringView extends AbstractList<String> implements RandomAccess {

        @Override
        public String get(int index) {
            return completions.get(index).suggestion();
        }

        @Override
        public String set(int index, String element) {
            return completions.set(index, Completion.completion(element)).suggestion();
        }

        @Override
        public void add(int index, String element) {
            completions.add(index, Completion.completion(element));
        }

        @Override
        public String remove(int index) {
            return completions.remove(index).suggestion();
        }

        @Override
        public void clear() {
            completions.clear();
        }

        @Override
        public int size() {
            return completions.size();
        }
    }

    /**
     * A rich tab completion, consisting of a string suggestion, and a nullable {@link Component} tooltip.
     */
    public interface Completion extends Examinable {

        @NotNull String suggestion();

        @Nullable Component tooltip();

        @Override
        default @NotNull Stream<? extends ExaminableProperty> examinableProperties() {
            return Stream.of(ExaminableProperty.of("suggestion", this.suggestion()), ExaminableProperty.of("tooltip", this.tooltip()));
        }

        static @NotNull Completion completion(final @NotNull String suggestion) {
            return new CompletionImpl(suggestion, null);
        }

        static @NotNull Completion completion(final @NotNull String suggestion, final @Nullable Component tooltip) {
            return new CompletionImpl(suggestion, tooltip);
        }
    }

    static final class CompletionImpl implements Completion {

        private final String suggestion;
        private final @Nullable Component tooltip;

        CompletionImpl(final String suggestion, final @Nullable Component tooltip) {
            this.suggestion = suggestion;
            this.tooltip = tooltip;
        }

        @Override
        public @NotNull String suggestion() {
            return this.suggestion;
        }

        @Override
        public @Nullable Component tooltip() {
            return this.tooltip;
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final CompletionImpl that = (CompletionImpl) o;
            return this.suggestion.equals(that.suggestion)
                && Objects.equals(this.tooltip, that.tooltip);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.suggestion, this.tooltip);
        }

        @Override
        public String toString() {
            return StringExaminer.simpleEscaping().examine(this);
        }
    }
}
