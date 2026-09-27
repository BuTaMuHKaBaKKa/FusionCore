package io.izzel.arclight.common.mod.util;

import com.destroystokyo.paper.Title;
import com.google.common.base.Preconditions;
import io.izzel.arclight.common.bridge.bukkit.CraftPlayerTabListBridge;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.craftbukkit.v.util.CraftChatMessage;
import org.bukkit.entity.Player;

/** Fusion Core: Paper's deprecated BungeeCord title and tab list methods on {@code Player}. */
public final class FusionCoreTitles {

    private FusionCoreTitles() {
    }

    private static Component toVanilla(BaseComponent[] components) {
        return CraftChatMessage.fromJSON(ComponentSerializer.toString(components));
    }

    private static void send(Player player, Packet<?> packet) {
        ServerPlayer handle = ((CraftPlayer) player).getHandle();
        if (handle.connection != null) {
            handle.connection.send(packet);
        }
    }

    public static void setPlayerListHeaderFooter(Player player, BaseComponent[] header, BaseComponent[] footer) {
        ((CraftPlayerTabListBridge) player).fusioncore$setPlayerListHeaderFooter(
            header == null ? null : toVanilla(header), footer == null ? null : toVanilla(footer));
    }

    public static void setTitleTimes(Player player, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        send(player, new ClientboundSetTitlesAnimationPacket(fadeInTicks, stayTicks, fadeOutTicks));
    }

    public static void setSubtitle(Player player, BaseComponent[] subtitle) {
        send(player, new ClientboundSetSubtitleTextPacket(toVanilla(subtitle)));
    }

    public static void showTitle(Player player, BaseComponent[] title) {
        send(player, new ClientboundSetTitleTextPacket(toVanilla(title)));
    }

    public static void showTitle(Player player, BaseComponent[] title, BaseComponent[] subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        setTitleTimes(player, fadeInTicks, stayTicks, fadeOutTicks);
        setSubtitle(player, subtitle);
        showTitle(player, title);
    }

    public static void send(Player player, Title title) {
        Preconditions.checkNotNull(title, "Title is null");
        setTitleTimes(player, title.getFadeIn(), title.getStay(), title.getFadeOut());
        setSubtitle(player, title.getSubtitle() == null ? new BaseComponent[0] : title.getSubtitle());
        showTitle(player, title.getTitle());
    }

    public static void update(Player player, Title title) {
        Preconditions.checkNotNull(title, "Title is null");
        setTitleTimes(player, title.getFadeIn(), title.getStay(), title.getFadeOut());
        if (title.getSubtitle() != null) {
            setSubtitle(player, title.getSubtitle());
        }
        showTitle(player, title.getTitle());
    }

    public static void hideTitle(Player player) {
        send(player, new ClientboundClearTitlesPacket(false));
    }
}
