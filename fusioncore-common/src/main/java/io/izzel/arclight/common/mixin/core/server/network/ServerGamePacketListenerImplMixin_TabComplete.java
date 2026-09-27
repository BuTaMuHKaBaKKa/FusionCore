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

/** Fusion Core: Paper's AsyncTabCompleteEvent for command suggestions, run off the main thread when someone listens. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_TabComplete {

    @Unique
    private static final ExecutorService fusioncore$TAB_COMPLETE_EXECUTOR = Executors.newFixedThreadPool(4,
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Async Tab Complete Thread - #%d")
            .setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(MinecraftServer.LOGGER)).build());

    @Inject(method = "handleCustomCommandSuggestions", cancellable = true, at = @At("HEAD"))
    private void fusioncore$asyncTabComplete(ServerboundCommandSuggestionPacket packet, CallbackInfo ci) {
        MinecraftServer server = ArclightServer.getMinecraftServer();
        if (server.isSameThread() || !ArclightServer.isInitialized()
            || AsyncTabCompleteEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }
        ci.cancel();
        fusioncore$TAB_COMPLETE_EXECUTOR.execute(() -> this.fusioncore$fireAsyncTabComplete(packet, server));
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
