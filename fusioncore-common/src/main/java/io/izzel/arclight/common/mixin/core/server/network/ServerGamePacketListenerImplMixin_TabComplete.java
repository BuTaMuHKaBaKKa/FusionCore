package io.izzel.arclight.common.mixin.core.server.network;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** Fusion Core: Paper's AsyncTabCompleteEvent for command suggestions, run off the main thread when someone listens. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_TabComplete {

    @Unique
    private static final ExecutorService fusioncore$TAB_COMPLETE_EXECUTOR = Executors.newFixedThreadPool(4,
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Async Tab Complete Thread - #%d")
            .setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(MinecraftServer.LOGGER)).build());

    // Paper's tab spam limiter (patches 0161, 0222): counted once per request, on the network thread, before anything else
    @Unique private final AtomicInteger fusioncore$tabSpam = new AtomicInteger();
    // requests of this player waiting for the async executor
    @Unique private final AtomicInteger fusioncore$tabPending = new AtomicInteger();

    @Inject(method = "tick", at = @At("HEAD"))
    private void fusioncore$tabSpamDecay(CallbackInfo ci) {
        if (this.fusioncore$tabSpam.get() > 0) {
            this.fusioncore$tabSpam.getAndDecrement();
        }
    }

    @Inject(method = "handleCustomCommandSuggestions", cancellable = true, at = @At("HEAD"))
    private void fusioncore$asyncTabComplete(ServerboundCommandSuggestionPacket packet, CallbackInfo ci) {
        MinecraftServer server = ArclightServer.getMinecraftServer();
        if (server.isSameThread() || !ArclightServer.isInitialized()) {
            return;
        }
        var self = (ServerGamePacketListenerImpl) (Object) this;
        var limits = io.izzel.arclight.i18n.ArclightConfig.spec().getSpamLimiter();
        if (this.fusioncore$tabSpam.addAndGet(limits.getTabSpamIncrement()) > limits.getTabSpamLimit()
            && !server.getPlayerList().isOp(self.player.getGameProfile())) {
            ci.cancel();
            server.execute(() -> self.disconnect(Component.translatable("disconnect.spam")));
            return;
        }
        if (AsyncTabCompleteEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }
        ci.cancel();
        // a slow listener must not let one player queue requests without end: over the limit, no suggestions
        if (!self.isAcceptingMessages() || this.fusioncore$tabPending.incrementAndGet() > limits.getTabCompleteMaxPending()) {
            if (self.isAcceptingMessages()) {
                this.fusioncore$tabPending.decrementAndGet();
            }
            return;
        }
        fusioncore$TAB_COMPLETE_EXECUTOR.execute(() -> {
            try {
                if (self.isAcceptingMessages()) {
                    this.fusioncore$fireAsyncTabComplete(packet, server);
                }
            } finally {
                this.fusioncore$tabPending.decrementAndGet();
            }
        });
    }

    @Unique
    private void fusioncore$fireAsyncTabComplete(ServerboundCommandSuggestionPacket packet, MinecraftServer server) {
        var self = (ServerGamePacketListenerImpl) (Object) this;
        StringReader stringreader = new StringReader(packet.getCommand());
        if (stringreader.canRead() && stringreader.peek() == '/') {
            stringreader.skip();
        }

        var event = new AsyncTabCompleteEvent(((ServerPlayerBridge) self.player).bridge$getBukkitEntity(), packet.getCommand(), true, null);
        Bukkit.getPluginManager().callEvent(event);
        final List<AsyncTabCompleteEvent.Completion> completions = event.isCancelled() ? List.of() : event.completions();
        if (!event.isHandled()) {
            if (event.isCancelled()) {
                return;
            }
            server.execute(() -> {
                if (self.isAcceptingMessages()) {
                    self.handleCustomCommandSuggestions(packet);
                }
            });
        } else if (!completions.isEmpty()) {
            var registries = self.player.registryAccess();
            SuggestionsBuilder builder0 = new SuggestionsBuilder(packet.getCommand(), stringreader.getTotalLength());
            SuggestionsBuilder builder = builder0.createOffset(builder0.getInput().lastIndexOf(' ') + 1);
            for (AsyncTabCompleteEvent.Completion completion : completions) {
                Component tooltip = completion.tooltip() == null ? null : FusionCoreAdventure.asVanilla(completion.tooltip(), registries);
                Integer intSuggestion = Ints.tryParse(completion.suggestion());
                if (intSuggestion != null) {
                    builder.suggest(intSuggestion, tooltip);
                } else {
                    builder.suggest(completion.suggestion(), tooltip);
                }
            }
            this.fusioncore$sendSuggestions(packet, builder.buildFuture().join());
        }
    }

    // Fusion Core: vanilla's reply to a suggestion request (a lambda, matched by descriptor) goes through
    // Paper's AsyncPlayerSendSuggestionsEvent
    @Inject(method = "*(Lnet/minecraft/network/protocol/game/ServerboundCommandSuggestionPacket;Lcom/mojang/brigadier/suggestion/Suggestions;)V",
        cancellable = true, at = @At("HEAD"))
    private void fusioncore$sendSuggestionsEvent(ServerboundCommandSuggestionPacket packet, Suggestions suggestions, CallbackInfo ci) {
        ci.cancel();
        this.fusioncore$sendSuggestions(packet, suggestions);
    }

    /** Paper's AsyncPlayerSendSuggestionsEvent: starts cancelled when empty, at most 1000 suggestions. */
    @Unique
    private void fusioncore$sendSuggestions(ServerboundCommandSuggestionPacket packet, Suggestions suggestions) {
        var self = (ServerGamePacketListenerImpl) (Object) this;
        var event = new AsyncPlayerSendSuggestionsEvent(((ServerPlayerBridge) self.player).bridge$getBukkitEntity(), suggestions, packet.getCommand());
        event.setCancelled(suggestions.isEmpty());
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            Suggestions sent = event.getSuggestions();
            if (sent.getList().size() > 1000) {
                sent = new Suggestions(sent.getRange(), sent.getList().subList(0, 1000));
            }
            self.send(new ClientboundCommandSuggestionsPacket(packet.getId(), sent));
        }
    }
}
