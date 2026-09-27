package io.izzel.arclight.common.mod.util;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.resource.ResourcePackCallback;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/** Fusion Core: Paper's Audience parts for players: boss bars, resource packs, books, chat types, message deletion. */
public final class FusionCoreAudiences {

    private FusionCoreAudiences() {
    }

    private static ServerPlayer handle(Player player) {
        return FusionCorePlayers.handle(player);
    }

    // boss bars: one vanilla ServerBossEvent per shown Adventure bar, kept in step by a bar listener (Paper's
    // BossBarImplementationImpl; Paper makes the vanilla getters read the bar, here the listener calls the setters,
    // which send the same update packets)

    private static final Map<BossBar, Bar> BARS = Collections.synchronizedMap(new WeakHashMap<>());

    private static final class Bar implements BossBar.Listener {
        private final ServerBossEvent vanilla;

        private Bar(BossBar bar) {
            this.vanilla = new ServerBossEvent(FusionCoreAdventure.asVanilla(bar.name()), color(bar.color()), overlay(bar.overlay()));
            this.vanilla.setProgress(bar.progress());
            this.flags(bar);
        }

        private void flags(BossBar bar) {
            this.vanilla.setDarkenScreen(bar.hasFlag(BossBar.Flag.DARKEN_SCREEN));
            this.vanilla.setPlayBossMusic(bar.hasFlag(BossBar.Flag.PLAY_BOSS_MUSIC));
            this.vanilla.setCreateWorldFog(bar.hasFlag(BossBar.Flag.CREATE_WORLD_FOG));
        }

        @Override
        public void bossBarNameChanged(BossBar bar, net.kyori.adventure.text.Component oldName, net.kyori.adventure.text.Component newName) {
            this.vanilla.setName(FusionCoreAdventure.asVanilla(newName));
        }

        @Override
        public void bossBarProgressChanged(BossBar bar, float oldProgress, float newProgress) {
            this.vanilla.setProgress(newProgress);
        }

        @Override
        public void bossBarColorChanged(BossBar bar, BossBar.Color oldColor, BossBar.Color newColor) {
            this.vanilla.setColor(color(newColor));
        }

        @Override
        public void bossBarOverlayChanged(BossBar bar, BossBar.Overlay oldOverlay, BossBar.Overlay newOverlay) {
            this.vanilla.setOverlay(overlay(newOverlay));
        }

        @Override
        public void bossBarFlagsChanged(BossBar bar, Set<BossBar.Flag> flagsAdded, Set<BossBar.Flag> flagsRemoved) {
            this.flags(bar);
        }
    }

    private static BossEvent.BossBarColor color(BossBar.Color color) {
        return switch (color) {
            case PINK -> BossEvent.BossBarColor.PINK;
            case BLUE -> BossEvent.BossBarColor.BLUE;
            case RED -> BossEvent.BossBarColor.RED;
            case GREEN -> BossEvent.BossBarColor.GREEN;
            case YELLOW -> BossEvent.BossBarColor.YELLOW;
            case PURPLE -> BossEvent.BossBarColor.PURPLE;
            case WHITE -> BossEvent.BossBarColor.WHITE;
        };
    }

    private static BossEvent.BossBarOverlay overlay(BossBar.Overlay overlay) {
        return switch (overlay) {
            case PROGRESS -> BossEvent.BossBarOverlay.PROGRESS;
            case NOTCHED_6 -> BossEvent.BossBarOverlay.NOTCHED_6;
            case NOTCHED_10 -> BossEvent.BossBarOverlay.NOTCHED_10;
            case NOTCHED_12 -> BossEvent.BossBarOverlay.NOTCHED_12;
            case NOTCHED_20 -> BossEvent.BossBarOverlay.NOTCHED_20;
        };
    }

    public static void showBossBar(Player player, BossBar bar) {
        Bar impl;
        synchronized (BARS) {
            impl = BARS.get(bar);
            if (impl == null) {
                impl = new Bar(bar);
                bar.addListener(impl);
                BARS.put(bar, impl);
            }
        }
        // Adventure's BossBar#viewers() asks the bar's implementation, which exists only once someone requests it (Paper
        // does in showBossBar); ours reads BARS through FusionCoreAdventureProviders
        net.kyori.adventure.bossbar.BossBarImplementation.get(bar, net.kyori.adventure.bossbar.BossBarImplementation.class);
        impl.vanilla.addPlayer(handle(player));
        FusionCorePlayers.state(player).fusioncore$bossBars().add(bar);
    }

    public static void hideBossBar(Player player, BossBar bar) {
        FusionCorePlayers.state(player).fusioncore$bossBars().remove(bar);
        Bar impl = BARS.get(bar);
        if (impl == null) {
            return;
        }
        // by UUID: the vanilla event may still hold the ServerPlayer from before a respawn
        for (ServerPlayer viewer : new ArrayList<>(impl.vanilla.getPlayers())) {
            if (viewer.getUUID().equals(player.getUniqueId())) {
                impl.vanilla.removePlayer(viewer);
            }
        }
        synchronized (BARS) {
            if (impl.vanilla.getPlayers().isEmpty() && BARS.get(bar) == impl) {
                bar.removeListener(impl);
                BARS.remove(bar);
            }
        }
    }

    /** Paper's {@code BossBarImplementationImpl#viewers}: the players the bar is shown to. */
    public static Iterable<? extends net.kyori.adventure.bossbar.BossBarViewer> viewers(BossBar bar) {
        Bar impl = BARS.get(bar);
        if (impl == null) {
            return Set.of();
        }
        List<net.kyori.adventure.bossbar.BossBarViewer> out = new ArrayList<>();
        for (ServerPlayer viewer : impl.vanilla.getPlayers()) {
            // Player is a BossBarViewer through PlayerMixin, which javac does not see
            out.add((net.kyori.adventure.bossbar.BossBarViewer) (Object) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) viewer).bridge$getBukkitEntity());
        }
        return Collections.unmodifiableList(out);
    }

    public static Iterable<? extends BossBar> activeBossBars(Player player) {
        return Collections.unmodifiableSet(FusionCorePlayers.state(player).fusioncore$bossBars());
    }

    /** NeoForge builds a new ServerPlayer on respawn: the bars it saw follow the new one. */
    public static void respawned(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        synchronized (BARS) {
            for (Bar impl : BARS.values()) {
                if (impl.vanilla.getPlayers().contains(oldPlayer)) {
                    impl.vanilla.removePlayer(oldPlayer);
                    impl.vanilla.addPlayer(newPlayer);
                }
            }
        }
    }

    /** A player that leaves is taken off every bar (Adventure keeps the bar, the vanilla event would keep the player). */
    public static void left(Player player) {
        for (BossBar bar : new ArrayList<>(FusionCorePlayers.state(player).fusioncore$bossBars())) {
            hideBossBar(player, bar);
        }
    }

    // resource packs (Paper patch 0010): push/pop packets, callbacks answered from the client's status

    public static void sendResourcePacks(Player player, ResourcePackRequest request) {
        ServerPlayer handle = handle(player);
        if (handle.connection == null) {
            return;
        }
        if (request.replace()) {
            clearResourcePacks(player);
        }
        net.minecraft.network.chat.Component prompt = request.prompt() == null ? null : FusionCoreAdventure.asVanilla(request.prompt());
        for (java.util.Iterator<ResourcePackInfo> it = request.packs().iterator(); it.hasNext(); ) {
            ResourcePackInfo pack = it.next();
            handle.connection.send(new ClientboundResourcePackPushPacket(pack.id(), pack.uri().toASCIIString(), pack.hash(), request.required(),
                it.hasNext() ? Optional.empty() : Optional.ofNullable(prompt)));
            if (request.callback() != ResourcePackCallback.noOp()) {
                FusionCorePlayers.state(player).fusioncore$packCallbacks().put(pack.id(), request.callback());
            }
        }
    }

    public static void removeResourcePacks(Player player, UUID id, UUID... others) {
        ServerPlayer handle = handle(player);
        if (handle.connection == null) {
            return;
        }
        handle.connection.send(new ClientboundResourcePackPopPacket(Optional.of(id)));
        for (UUID other : others) {
            handle.connection.send(new ClientboundResourcePackPopPacket(Optional.of(other)));
        }
    }

    public static void clearResourcePacks(Player player) {
        ServerPlayer handle = handle(player);
        if (handle.connection != null) {
            handle.connection.send(new ClientboundResourcePackPopPacket(Optional.empty()));
        }
    }

    /** Before PlayerResourcePackStatusEvent, as in Paper; a terminal status drops the callback. */
    public static void packStatus(Player player, UUID id, net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action action) {
        Map<UUID, ResourcePackCallback> callbacks = FusionCorePlayers.state(player).fusioncore$packCallbacks();
        ResourcePackCallback callback = action.isTerminal() ? callbacks.remove(id) : callbacks.get(id);
        if (callback != null) {
            callback.packEventReceived(id, net.kyori.adventure.resource.ResourcePackStatus.valueOf(action.name()), (net.kyori.adventure.audience.Audience) (Object) player);
        }
    }

    /** Paper: the book is shown from a fake main-hand slot, then the real item is put back. */
    public static void openBook(Player player, Book book) {
        ServerPlayer handle = handle(player);
        if (handle.connection == null) {
            return;
        }
        String title = FusionCoreAdventure.asPlain(book.title());
        if (title.length() > WrittenBookContent.TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Field 'title' has a maximum length of " + WrittenBookContent.TITLE_MAX_LENGTH + " but was passed '" + title
                + "', which was " + title.length() + " characters long.");
        }
        ItemStack item = new ItemStack(Items.WRITTEN_BOOK, 1);
        item.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(title), FusionCoreAdventure.asPlain(book.author()), 0,
            book.pages().stream().map(page -> Filterable.passThrough(FusionCoreAdventure.asVanilla(page))).toList(), false));
        net.minecraft.world.entity.player.Inventory inventory = handle.getInventory();
        int slot = inventory.items.size() + inventory.selected;
        int stateId = handle.containerMenu.getStateId();
        handle.connection.send(new ClientboundContainerSetSlotPacket(0, stateId, slot, item));
        handle.connection.send(new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND));
        handle.connection.send(new ClientboundContainerSetSlotPacket(0, stateId, slot, inventory.getSelected()));
    }

    /** Paper: a disguised chat message decorated by the bound chat type (e.g. {@code <name> message}). */
    public static void sendMessage(Player player, net.kyori.adventure.text.Component message, net.kyori.adventure.chat.ChatType.Bound bound) {
        ServerPlayer handle = handle(player);
        if (handle.connection == null) {
            return;
        }
        var registry = handle.level().registryAccess().registryOrThrow(Registries.CHAT_TYPE);
        var type = registry.getHolderOrThrow(ResourceKey.create(Registries.CHAT_TYPE, ResourceLocation.parse(bound.type().key().asString())));
        net.minecraft.network.chat.ChatType.Bound vanilla = new net.minecraft.network.chat.ChatType.Bound(type, FusionCoreAdventure.asVanilla(bound.name()),
            Optional.ofNullable(bound.target() == null ? null : FusionCoreAdventure.asVanilla(bound.target())));
        handle.sendChatMessage(new OutgoingChatMessage.Disguised(FusionCoreAdventure.asVanilla(message)), handle.isTextFilteringEnabled(), vanilla);
    }

    public static void deleteMessage(Player player, net.kyori.adventure.chat.SignedMessage.Signature signature) {
        ServerPlayer handle = handle(player);
        if (handle.connection != null) {
            handle.connection.send(new net.minecraft.network.protocol.game.ClientboundDeleteChatPacket(new MessageSignature.Packed(new MessageSignature(signature.bytes()))));
        }
    }

    // pointers (Paper patch 0010): what Adventure asks an audience about itself

    public static net.kyori.adventure.util.TriState permissionValue(org.bukkit.permissions.Permissible permissible, String permission) {
        return permissible.isPermissionSet(permission) ? net.kyori.adventure.util.TriState.byBoolean(permissible.hasPermission(permission))
            : net.kyori.adventure.util.TriState.NOT_SET;
    }

    public static net.kyori.adventure.pointer.Pointers entityPointers(org.bukkit.entity.Entity entity) {
        net.minecraft.world.entity.Entity handle = ((org.bukkit.craftbukkit.v.entity.CraftEntity) entity).getHandle();
        net.kyori.adventure.pointer.Pointers.Builder builder = net.kyori.adventure.pointer.Pointers.builder()
            .withDynamic(net.kyori.adventure.identity.Identity.DISPLAY_NAME, () -> FusionCoreAdventure.asAdventure(handle.getDisplayName()))
            .withDynamic(net.kyori.adventure.identity.Identity.UUID, entity::getUniqueId)
            .withStatic(net.kyori.adventure.permission.PermissionChecker.POINTER, permission -> permissionValue(entity, permission));
        if (entity instanceof Player player) {
            builder.withDynamic(net.kyori.adventure.identity.Identity.NAME, player::getName)
                .withDynamic(net.kyori.adventure.identity.Identity.LOCALE, () -> net.kyori.adventure.translation.Translator.parseLocale(player.getLocale()));
        }
        return builder.build();
    }
}
