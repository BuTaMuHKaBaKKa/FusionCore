package io.izzel.arclight.common.mod.util;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.google.common.base.Preconditions;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.server.TabCompleteEvent;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// see net.minecraftforge.server.console.TerminalHandler
public class ArclightTerminalReader {

    public static boolean handleCommands(DedicatedServer server) {
        var terminal = TerminalConsoleAppender.getTerminal();
        if (terminal == null)
            return false;

        var reader = LineReaderBuilder.builder()
            .appName("Fusion Core")
            .terminal(terminal)
            .completer(new ConsoleCommandCompleter(server))
            .build();
        reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        reader.unsetOpt(LineReader.Option.INSERT_TAB);

        TerminalConsoleAppender.setReader(reader);

        try {
            String line;
            while (!server.isStopped() && server.isRunning()) {
                try {
                    line = reader.readLine("> ");
                } catch (EndOfFileException ignored) {
                    // Continue reading after EOT
                    continue;
                }

                if (line == null)
                    break;

                line = line.trim();
                if (!line.isEmpty()) {
                    server.handleConsoleInput(line, server.createCommandSourceStack());
                }
            }
        } catch (UserInterruptException e) {
            // DONT USE CLOSE, YOU AREN'T GOING TO SHUT IT DOWN IN ANOTHER THREAD!
            server.halt(true);
        } finally {
            TerminalConsoleAppender.setReader(null);
        }
        return true;
    }

    static final class ConsoleCommandCompleter implements Completer {
        private static final Logger logger = LogManager.getLogger();
        private final DedicatedServer server;

        public ConsoleCommandCompleter(DedicatedServer server) {
            this.server = Preconditions.checkNotNull(server, "server");
        }

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            String buffer = line.line();
            boolean prefix;
            if (buffer.isEmpty() || buffer.charAt(0) != '/') {
                buffer = '/' + buffer;
                prefix = false;
            } else {
                prefix = true;
            }

            final String input = buffer;
            if (this.completeAsync(input, candidates)) {
                return;
            }
            //See NetHandlerPlayServer#processTabComplete
            StringReader stringReader = new StringReader(input);
            if (stringReader.canRead() && stringReader.peek() == '/')
                stringReader.skip();

            try {
                ParseResults<CommandSourceStack> results = this.server.getCommands().getDispatcher().parse(stringReader, this.server.createCommandSourceStack());
                Suggestions tabComplete = this.server.getCommands().getDispatcher().getCompletionSuggestions(results).get();
                for (Suggestion suggestion : tabComplete.getList()) {
                    String completion = suggestion.getText();
                    if (!completion.isEmpty()) {
                        boolean hasPrefix = prefix || completion.charAt(0) != '/';
                        Candidate candidate = new Candidate(hasPrefix ? completion : completion.substring(1));
                        candidates.add(candidate);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                logger.error("Failed to tab complete", e);
            }
        }

        /**
         * Fusion Core: Paper's AsyncTabCompleteEvent for the console (Paper's ConsoleCommandCompleter).
         * Returns {@code true} when the event cancelled or handled the completion.
         */
        private boolean completeAsync(String buffer, List<Candidate> candidates) {
            if (!ArclightServer.isInitialized()
                || AsyncTabCompleteEvent.getHandlerList().getRegisteredListeners().length == 0) {
                return false;
            }
            var sender = Bukkit.getConsoleSender();
            var event = new AsyncTabCompleteEvent(sender, buffer, true, null);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled() && !event.isHandled()) {
                return false;
            }
            final List<AsyncTabCompleteEvent.Completion> completions = event.isCancelled() ? new ArrayList<>() : event.completions();
            // Still fire the sync event with the provided completions, if someone is listening
            if (!event.isCancelled() && TabCompleteEvent.getHandlerList().getRegisteredListeners().length > 0) {
                var offered = completions.stream().map(AsyncTabCompleteEvent.Completion::suggestion).collect(Collectors.toList());
                var sync = new CompletableFuture<List<String>>();
                ArclightServer.executeOnMainThread(() -> {
                    try {
                        var syncEvent = new TabCompleteEvent(sender, buffer, offered);
                        Bukkit.getPluginManager().callEvent(syncEvent);
                        sync.complete(syncEvent.isCancelled() ? List.of() : syncEvent.getCompletions());
                    } catch (Throwable t) {
                        sync.completeExceptionally(t);
                    }
                });
                try {
                    final List<String> legacy = sync.get();
                    completions.removeIf(it -> !legacy.contains(it.suggestion()));
                    for (String completion : legacy) {
                        if (completions.stream().noneMatch(it -> it.suggestion().equals(completion))) {
                            completions.add(AsyncTabCompleteEvent.Completion.completion(completion));
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    logger.error("Failed to tab complete", e);
                }
            }
            for (AsyncTabCompleteEvent.Completion completion : completions) {
                if (completion.suggestion().isEmpty()) {
                    continue;
                }
                String tooltip = completion.tooltip() == null ? null
                    : PlainTextComponentSerializer.plainText().serialize(completion.tooltip());
                candidates.add(new Candidate(completion.suggestion(), completion.suggestion(), null, tooltip, null, null, false));
            }
            return true;
        }

    }
}
