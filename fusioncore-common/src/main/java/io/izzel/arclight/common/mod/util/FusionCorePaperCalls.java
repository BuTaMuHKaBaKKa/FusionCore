package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.server.ArclightServer;
import io.papermc.paper.advancement.AdvancementDisplay;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.HolderLookup;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Fusion Core: Paper API calls that cannot be added to Bukkit types (return-type clashes with Spigot, public statics on
 * {@code Bukkit}); {@code PaperCallTransformer} redirects plugin calls here.
 */
public final class FusionCorePaperCalls {

    private FusionCorePaperCalls() {
    }

    public static @Nullable AdvancementDisplay advancementDisplay(Advancement advancement) {
        AdvancementHolder holder = ((CraftAdvancement) advancement).getHandle();
        return holder.value().display()
            .map(info -> (AdvancementDisplay) new Display(info, holder.value().name().orElse(info.getTitle())))
            .orElse(null);
    }

    public static com.destroystokyo.paper.profile.PlayerProfile playerProfile(org.bukkit.OfflinePlayer player) {
        return FusionCoreProfiles.playerProfile(player);
    }

    public static int broadcast(Component message) {
        return broadcast(message, Server.BROADCAST_CHANNEL_USERS);
    }

    /** Like CraftBukkit's {@code broadcast(String, String)}, but each recipient gets the component. */
    public static int broadcast(Component message, String permission) {
        Set<CommandSender> recipients = new LinkedHashSet<>();
        for (Permissible permissible : Bukkit.getPluginManager().getPermissionSubscriptions(permission)) {
            if (permissible instanceof CommandSender sender && permissible.hasPermission(permission)) {
                recipients.add(sender);
            }
        }
        String legacy = FusionCoreAdventure.toLegacyRgb(message);
        BroadcastMessageEvent event = new BroadcastMessageEvent(!Bukkit.isPrimaryThread(), legacy, recipients);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return 0;
        }
        Component toSend = legacy.equals(event.getMessage()) ? message : FusionCoreAdventure.fromLegacy(event.getMessage());
        for (CommandSender recipient : recipients) {
            ((Audience) (Object) recipient).sendMessage(toSend);
        }
        return recipients.size();
    }

    private static HolderLookup.Provider registries() {
        return ArclightServer.getMinecraftServer().registryAccess();
    }

    /** Port of Paper's {@code PaperAdvancementDisplay}. */
    private record Display(DisplayInfo handle, net.minecraft.network.chat.Component name) implements AdvancementDisplay {

        @Override
        public @NotNull Frame frame() {
            return switch (this.handle.getType()) {
                case TASK -> Frame.TASK;
                case CHALLENGE -> Frame.CHALLENGE;
                case GOAL -> Frame.GOAL;
            };
        }

        @Override
        public @NotNull Component title() {
            return FusionCoreAdventure.asAdventure(this.handle.getTitle(), registries());
        }

        @Override
        public @NotNull Component description() {
            return FusionCoreAdventure.asAdventure(this.handle.getDescription(), registries());
        }

        @Override
        public @NotNull ItemStack icon() {
            return CraftItemStack.asBukkitCopy(this.handle.getIcon());
        }

        @Override
        public boolean doesShowToast() {
            return this.handle.shouldShowToast();
        }

        @Override
        public boolean doesAnnounceToChat() {
            return this.handle.shouldAnnounceChat();
        }

        @Override
        public boolean isHidden() {
            return this.handle.isHidden();
        }

        @Override
        public @Nullable NamespacedKey backgroundPath() {
            return this.handle.getBackground().map(CraftNamespacedKey::fromMinecraft).orElse(null);
        }

        @Override
        public @NotNull Component displayName() {
            return FusionCoreAdventure.asAdventure(this.name, registries());
        }
    }

    // Paper's BookMeta methods that return BookMeta where the existing ones return Adventure's Book
    // (see BookMetaMixin): each delegates and hands back the same meta.

    public static org.bukkit.inventory.meta.BookMeta bookTitle(org.bukkit.inventory.meta.BookMeta meta, Component title) {
        ((net.kyori.adventure.inventory.Book) (Object) meta).title(title);
        return meta;
    }

    public static org.bukkit.inventory.meta.BookMeta bookAuthor(org.bukkit.inventory.meta.BookMeta meta, Component author) {
        ((net.kyori.adventure.inventory.Book) (Object) meta).author(author);
        return meta;
    }

    public static org.bukkit.inventory.meta.BookMeta bookPages(org.bukkit.inventory.meta.BookMeta meta, java.util.List<Component> pages) {
        ((net.kyori.adventure.inventory.Book) (Object) meta).pages(pages);
        return meta;
    }

    public static org.bukkit.inventory.meta.BookMeta bookPagesArray(org.bukkit.inventory.meta.BookMeta meta, Component[] pages) {
        ((net.kyori.adventure.inventory.Book) (Object) meta).pages(pages);
        return meta;
    }

    public static org.bukkit.inventory.meta.BookMeta buildBook(org.bukkit.inventory.meta.BookMeta$BookMetaBuilder builder) {
        return (org.bukkit.inventory.meta.BookMeta) (Object) builder.build();
    }
}
