package io.izzel.arclight.common.mod.util;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.network.StatusClient;
import com.mojang.authlib.GameProfile;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.network.ConnectionBridge;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Fusion Core: fires {@link PaperServerListPingEvent} for server list pings, as Paper does. */
public final class FusionCoreServerListPing extends PaperServerListPingEvent {

    private final MinecraftServer server;
    private List<GameProfile> originalSample;

    public FusionCoreServerListPing(MinecraftServer server, Connection connection, List<GameProfile> sample) {
        super(((ConnectionBridge) connection).bridge$getHostname(), new Client(connection), server.getMotd(),
            server.getPlayerCount(), server.getMaxPlayers(),
            server.getServerModName() + " " + server.getServerVersion(),
            SharedConstants.getCurrentVersion().getProtocolVersion(),
            ((CraftServer) org.bukkit.Bukkit.getServer()).getServerIcon());
        this.server = server;
        this.originalSample = sample;
    }

    @NotNull
    @Override
    public List<ListedPlayerInfo> getListedPlayers() {
        List<ListedPlayerInfo> sample = super.getListedPlayers();
        if (this.originalSample != null) {
            for (GameProfile profile : this.originalSample) {
                sample.add(new ListedPlayerInfo(profile.getName(), profile.getId()));
            }
            this.originalSample = null;
        }
        return sample;
    }

    @SuppressWarnings("removal")
    @NotNull
    @Override
    public List<com.destroystokyo.paper.profile.PlayerProfile> getPlayerSample() {
        this.getListedPlayers(); // populate the backing list of the view
        return super.getPlayerSample();
    }

    /** The sample to send, without converting it if no plugin touched it. */
    public List<GameProfile> getPlayerSampleHandle() {
        if (this.originalSample != null) {
            return this.originalSample;
        }
        List<ListedPlayerInfo> entries = super.getListedPlayers();
        if (entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<GameProfile> profiles = new ArrayList<>(entries.size());
        for (ListedPlayerInfo info : entries) {
            profiles.add(new GameProfile(info.id(), info.name()));
        }
        return profiles;
    }

    @NotNull
    @Override
    protected Object[] getOnlinePlayers() {
        return this.server.getPlayerList().players.toArray();
    }

    @NotNull
    @Override
    protected Player getBukkitPlayer(@NotNull Object player) {
        return (Player) ((EntityBridge) (ServerPlayer) player).bridge$getBukkitEntity();
    }

    private record Client(Connection connection) implements StatusClient {

        @NotNull
        @Override
        public InetSocketAddress getAddress() {
            return (InetSocketAddress) this.connection.getRemoteAddress();
        }

        @Override
        public int getProtocolVersion() {
            return ((ConnectionBridge) this.connection).bridge$getProtocolVersion();
        }

        @Override
        public InetSocketAddress getVirtualHost() {
            return ((ConnectionBridge) this.connection).bridge$getVirtualHost();
        }
    }
}
