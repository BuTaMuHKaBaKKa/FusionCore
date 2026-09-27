package io.izzel.arclight.common.mod.util;

import com.google.common.collect.ForwardingSet;
import io.izzel.arclight.common.bridge.core.server.MinecraftServerBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AbstractChatEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.craftbukkit.v.util.LazyPlayerSet;
import org.bukkit.craftbukkit.v.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

/**
 * Fusion Core: player chat as Paper does it ({@code ChatProcessor}): Spigot's chat events, then {@link AsyncChatEvent} and
 * {@link ChatEvent}. The raw chat type is {@code fusioncore:raw}.
 */
public final class FusionCoreChatProcessor {

    private static final Logger LOGGER = LogManager.getLogger("Fusion Core");
    static final ResourceKey<ChatType> RAW = ResourceKey.create(Registries.CHAT_TYPE, ResourceLocation.fromNamespaceAndPath("fusioncore", "raw"));
    static final String DEFAULT_LEGACY_FORMAT = "<%1$s> %2$s"; // copied from PlayerChatEvent/AsyncPlayerChatEvent
    private static volatile boolean rawMissingReported;

    private final MinecraftServer server;
    private final ServerPlayer player;
    private final PlayerChatMessage message;
    private final boolean async;
    private final RegistryAccess registries;
    private final String legacyOriginalMessage;
    private final Component originalMessage;
    private final FusionCoreSignedMessage signedMessage;

    private boolean messageChanged;
    private boolean formatChanged;

    public FusionCoreChatProcessor(MinecraftServer server, ServerPlayer player, PlayerChatMessage message, boolean async) {
        this.server = server;
        this.player = player;
        this.message = message;
        this.async = async;
        this.registries = server.registryAccess();
        // unsignedContent carries what mods did in ServerChatEvent; legacy listeners see that, not the raw text
        this.legacyOriginalMessage = message.unsignedContent() != null
            ? legacySection().serialize(FusionCoreAdventure.asAdventure(message.unsignedContent(), this.registries))
            : message.signedContent();
        this.originalMessage = FusionCoreAdventure.asAdventure(message.decoratedContent(), this.registries);
        this.signedMessage = new FusionCoreSignedMessage(message, this.registries);
    }

    @SuppressWarnings("deprecation")
    public void process() {
        final boolean listenersOnAsyncEvent = canYouHearMe(AsyncPlayerChatEvent.getHandlerList());
        final boolean listenersOnSyncEvent = canYouHearMe(PlayerChatEvent.getHandlerList());
        if (listenersOnAsyncEvent || listenersOnSyncEvent) {
            final CraftPlayer craftPlayer = bukkit(this.player);
            final AsyncPlayerChatEvent ae = new AsyncPlayerChatEvent(this.async, craftPlayer, this.legacyOriginalMessage, new LazyPlayerSet(this.server));
            this.post(ae);
            if (listenersOnSyncEvent) {
                final PlayerChatEvent se = new PlayerChatEvent(craftPlayer, ae.getMessage(), ae.getFormat(), ae.getRecipients());
                se.setCancelled(ae.isCancelled()); // propagate cancelled state
                this.queueIfAsyncOrRunImmediately(new Waitable<Void>() {
                    @Override
                    protected Void evaluate() {
                        FusionCoreChatProcessor.this.post(se);
                        return null;
                    }
                });
                this.readLegacyModifications(se.getMessage(), se.getFormat());
                this.processModern(this.modernRenderer(se.getFormat()), this.viewersFromLegacy(se.getRecipients()),
                    this.modernMessage(se.getMessage()), se.getPlayer(), se.isCancelled());
            } else {
                this.readLegacyModifications(ae.getMessage(), ae.getFormat());
                this.processModern(this.modernRenderer(ae.getFormat()), this.viewersFromLegacy(ae.getRecipients()),
                    this.modernMessage(ae.getMessage()), ae.getPlayer(), ae.isCancelled());
            }
        } else {
            this.processModern(defaultRenderer(), new LazyAudienceSet(this.server), this.originalMessage, bukkit(this.player), false);
        }
    }

    private ChatRenderer modernRenderer(final String format) {
        return this.formatChanged ? legacyRenderer(format) : defaultRenderer();
    }

    private Component modernMessage(final String legacyMessage) {
        return this.messageChanged ? legacySection().deserialize(legacyMessage) : this.originalMessage;
    }

    private void readLegacyModifications(final String message, final String format) {
        this.messageChanged = !message.equals(this.legacyOriginalMessage);
        this.formatChanged = !format.equals(DEFAULT_LEGACY_FORMAT);
    }

    @SuppressWarnings("deprecation")
    private void processModern(final ChatRenderer renderer, final Set<Audience> viewers, final Component message, final Player player, final boolean cancelled) {
        final AsyncChatEvent ae = new AsyncChatEvent(this.async, player, viewers, renderer, message, this.originalMessage, this.signedMessage);
        ae.setCancelled(cancelled); // propagate cancelled state
        this.post(ae);
        if (canYouHearMe(ChatEvent.getHandlerList())) {
            this.queueIfAsyncOrRunImmediately(new Waitable<Void>() {
                @Override
                protected Void evaluate() {
                    final ChatEvent se = new ChatEvent(player, ae.viewers(), ae.renderer(), ae.message(), FusionCoreChatProcessor.this.originalMessage, FusionCoreChatProcessor.this.signedMessage);
                    se.setCancelled(ae.isCancelled()); // propagate cancelled state
                    FusionCoreChatProcessor.this.post(se);
                    FusionCoreChatProcessor.this.readModernModifications(se, renderer);
                    FusionCoreChatProcessor.this.complete(se);
                    return null;
                }
            });
        } else {
            this.readModernModifications(ae, renderer);
            this.complete(ae);
        }
    }

    private void readModernModifications(final AbstractChatEvent chatEvent, final ChatRenderer originalRenderer) {
        this.messageChanged = !chatEvent.message().equals(this.originalMessage);
        if (originalRenderer != chatEvent.renderer()) { // don't set to false if it hasn't changed
            this.formatChanged = true;
        }
    }

    private void complete(final AbstractChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final CraftPlayer sender = (CraftPlayer) event.getPlayer();
        final Component displayName = displayName(sender);
        final Component message = event.message();
        final ChatRenderer renderer = event.renderer();
        final Set<Audience> viewers = event.viewers();
        final boolean defaultType = renderer instanceof ChatRenderer.Default;
        final ChatType.Bound chatType = this.bind(defaultType ? ChatType.CHAT : RAW, sender, displayName);
        final Outgoing outgoing = viewers instanceof LazyAudienceSet lazy && lazy.isLazy() ? new ServerOutgoing() : new ViewersOutgoing(viewers);

        if (this.formatChanged) {
            if (renderer instanceof ChatRenderer.ViewerUnaware unaware) {
                final net.minecraft.network.chat.Component rendered = this.vanilla(unaware.render(sender, displayName, message));
                outgoing.send(chatType, viewer -> rendered);
            } else {
                outgoing.send(chatType, viewer -> this.vanilla(renderer.render(sender, displayName, message, viewer)));
            }
        } else if (this.messageChanged) {
            if (!(renderer instanceof ChatRenderer.ViewerUnaware unaware)) {
                throw new IllegalStateException("BUG: This should be a ViewerUnaware renderer at this point");
            }
            final Component rendered = defaultType ? message : unaware.render(sender, displayName, message);
            final net.minecraft.network.chat.Component vanilla = this.vanilla(rendered);
            outgoing.send(chatType, viewer -> vanilla);
        } else {
            outgoing.send(chatType, null);
        }
    }

    /**
     * Returns {@code null} only for {@link #RAW} when the running world has no such chat type
     * (the data pack in the mod jar did not load); callers then fall back to system messages.
     */
    private ChatType.Bound bind(ResourceKey<ChatType> key, CraftPlayer displaySource, Component displayName) {
        if (key == ChatType.CHAT && displaySource.getName().equals(displaySource.getDisplayName())) {
            return ChatType.bind(ChatType.CHAT, displaySource.getHandle());
        }
        if (this.registries.registryOrThrow(Registries.CHAT_TYPE).getHolder(key).isEmpty()) {
            if (!rawMissingReported) {
                rawMissingReported = true;
                LOGGER.warn("Chat type {} is missing; chat with a plugin-changed format is sent as system messages", key.location());
            }
            return null;
        }
        return ChatType.bind(key, this.registries, this.vanilla(displayName));
    }

    /** Where the message goes; {@code render} is {@code null} for the message as the player signed it. */
    private interface Outgoing {
        void send(ChatType.Bound chatType, Function<Audience, net.minecraft.network.chat.Component> render);
    }

    /** Nobody touched the viewer set: every player plus the console. */
    private final class ServerOutgoing implements Outgoing {

        @Override
        public void send(ChatType.Bound chatType, Function<Audience, net.minecraft.network.chat.Component> render) {
            final FusionCoreChatProcessor self = FusionCoreChatProcessor.this;
            if (chatType == null) {
                self.server.sendSystemMessage(render.apply(audience(Bukkit.getConsoleSender())));
                for (ServerPlayer viewer : self.server.getPlayerList().getPlayers()) {
                    viewer.sendSystemMessage(render.apply(audience(bukkit(viewer))));
                }
            } else if (render == null) {
                self.server.getPlayerList().broadcastChatMessage(self.message, self.player, chatType);
            } else {
                // Paper extends PlayerList#broadcastChatMessage with a per-viewer function; same loop here
                self.logToConsole(render.apply(audience(Bukkit.getConsoleSender())), chatType);
                boolean fullyFiltered = false;
                for (ServerPlayer viewer : self.server.getPlayerList().getPlayers()) {
                    fullyFiltered |= self.sendTo(viewer, render.apply(audience(bukkit(viewer))), chatType);
                }
                if (fullyFiltered) {
                    self.player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("chat.filtered_full"));
                }
            }
        }
    }

    /** A plugin replaced or read the viewer set: deliver to exactly those audiences. */
    private final class ViewersOutgoing implements Outgoing {

        private final Set<Audience> viewers;

        ViewersOutgoing(Set<Audience> viewers) {
            this.viewers = viewers;
        }

        @Override
        public void send(ChatType.Bound chatType, Function<Audience, net.minecraft.network.chat.Component> render) {
            for (Audience viewer : this.viewers) {
                this.sendTo(viewer, viewer, chatType, render);
            }
        }

        private void sendTo(Audience viewer, Audience renderFor, ChatType.Bound chatType, Function<Audience, net.minecraft.network.chat.Component> render) {
            final FusionCoreChatProcessor self = FusionCoreChatProcessor.this;
            final net.minecraft.network.chat.Component unsigned = render == null ? null : render.apply(renderFor);
            if (viewer instanceof ForwardingAudience.Single single) {
                this.sendTo(single.audience(), renderFor, chatType, render);
            } else if (viewer instanceof CraftPlayer craftPlayer) {
                if (chatType == null) {
                    craftPlayer.getHandle().sendSystemMessage(unsigned);
                } else {
                    self.sendTo(craftPlayer.getHandle(), unsigned, chatType);
                }
            } else if (viewer instanceof ConsoleCommandSender) {
                if (chatType == null) {
                    self.server.sendSystemMessage(unsigned);
                } else {
                    self.logToConsole(unsigned == null ? self.message.decoratedContent() : unsigned, chatType);
                }
            } else if (chatType == null) {
                viewer.sendMessage(self.adventure(unsigned));
            } else {
                final PlayerChatMessage msg = unsigned == null ? self.message : self.message.withUnsignedContent(unsigned);
                viewer.sendMessage(new FusionCoreSignedMessage(msg, self.registries), self.adventure(chatType));
            }
        }
    }

    /**
     * Sends the message like {@code ServerPlayer#sendChatMessage}, with Paper's unsigned override.
     *
     * @return whether the message was fully filtered for this viewer
     */
    private boolean sendTo(ServerPlayer viewer, net.minecraft.network.chat.Component unsigned, ChatType.Bound chatType) {
        if (viewer.getChatVisibility() != ChatVisiblity.FULL) {
            return false;
        }
        final boolean filter = this.player.shouldFilterMessageTo(viewer);
        if (this.message.isSystem()) {
            viewer.connection.sendDisguisedChatMessage(unsigned != null ? unsigned : this.message.decoratedContent(), chatType);
            return false;
        }
        PlayerChatMessage msg = this.message.filter(filter);
        if (unsigned != null) {
            msg = msg.withUnsignedContent(unsigned);
        }
        if (!msg.isFullyFiltered()) {
            viewer.connection.sendPlayerChatMessage(msg, chatType);
        }
        return filter && this.message.isFullyFiltered();
    }

    private void logToConsole(net.minecraft.network.chat.Component content, ChatType.Bound chatType) {
        final boolean trusted = this.message.hasSignature() && !this.message.hasExpiredServer(Instant.now());
        this.server.logChatMessage(content, chatType, trusted ? null : "Not Secure");
    }

    private net.kyori.adventure.chat.ChatType.Bound adventure(ChatType.Bound chatType) {
        final String key = chatType.chatType().unwrapKey().orElseThrow().location().toString();
        return net.kyori.adventure.chat.ChatType.chatType(Key.key(key)).bind(
            this.adventure(chatType.name()),
            chatType.targetName().map(this::adventure).orElse(null));
    }

    private Component adventure(net.minecraft.network.chat.Component component) {
        return FusionCoreAdventure.asAdventure(component, this.registries);
    }

    private net.minecraft.network.chat.Component vanilla(Component component) {
        return FusionCoreAdventure.asVanilla(component, this.registries);
    }

    private Set<Audience> viewersFromLegacy(final Set<Player> recipients) {
        if (recipients instanceof LazyPlayerSet lazyPlayerSet && lazyPlayerSet.isLazy()) {
            return new LazyAudienceSet(this.server);
        }
        final HashSet<Audience> viewers = new HashSet<>();
        for (Player recipient : recipients) {
            viewers.add(audience(recipient));
        }
        viewers.add(audience(Bukkit.getConsoleSender()));
        return viewers;
    }

    private static Component displayName(final CraftPlayer player) {
        final String legacy = player.getDisplayName();
        return player.getName().equals(legacy) ? Component.text(legacy) : legacySection().deserialize(legacy);
    }

    private static ChatRenderer.Default defaultRenderer() {
        return (ChatRenderer.Default) ChatRenderer.defaultRenderer();
    }

    private static ChatRenderer legacyRenderer(final String format) {
        if (DEFAULT_LEGACY_FORMAT.equals(format)) {
            return defaultRenderer();
        }
        return ChatRenderer.viewerUnaware((player, sourceDisplayName, message) ->
            legacySection().deserialize(String.format(format, player.getDisplayName(), legacySection().serialize(message))));
    }

    private void queueIfAsyncOrRunImmediately(final Waitable<Void> waitable) {
        if (this.async) {
            ((MinecraftServerBridge) this.server).bridge$queuedProcess(waitable);
        } else {
            waitable.run();
        }
        try {
            waitable.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            throw new RuntimeException("Exception processing chat", e.getCause());
        }
    }

    private void post(final Event event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    private static boolean canYouHearMe(final HandlerList handlers) {
        return handlers.getRegisteredListeners().length > 0;
    }

    /** {@code CommandSenderMixin} makes every sender an {@link Audience}; javac cannot see that. */
    private static Audience audience(CommandSender sender) {
        return (Audience) (Object) sender;
    }

    private static CraftPlayer bukkit(ServerPlayer player) {
        return ((ServerPlayerBridge) player).bridge$getBukkitEntity();
    }

    /** Every online player plus the console, materialised only when a listener touches it. */
    static final class LazyAudienceSet extends ForwardingSet<Audience> {

        private final MinecraftServer server;
        private Set<Audience> reference;

        LazyAudienceSet(MinecraftServer server) {
            this.server = server;
        }

        @Override
        protected Set<Audience> delegate() {
            if (this.reference == null) {
                final HashSet<Audience> audiences = new HashSet<>();
                for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
                    audiences.add(audience(bukkit(player)));
                }
                audiences.add(audience(Bukkit.getConsoleSender()));
                this.reference = audiences;
            }
            return this.reference;
        }

        boolean isLazy() {
            return this.reference == null;
        }
    }
}
