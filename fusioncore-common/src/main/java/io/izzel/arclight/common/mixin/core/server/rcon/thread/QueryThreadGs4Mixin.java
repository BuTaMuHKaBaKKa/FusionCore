package io.izzel.arclight.common.mixin.core.server.rcon.thread;

import com.destroystokyo.paper.event.server.GS4QueryEvent;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.rcon.NetworkDataOutputStream;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** Fusion Core: Paper's {@code GS4QueryEvent} (patch 0281); plugins are listed only if {@code query-plugins} allows it. */
@Mixin(QueryThreadGs4.class)
public abstract class QueryThreadGs4Mixin {

    // @formatter:off
    @Shadow @Final private int serverPort;
    @Shadow @Final private int maxPlayers;
    @Shadow @Final private String serverName;
    @Shadow @Final private String worldName;
    @Shadow private String hostIp;
    @Shadow @Final private NetworkDataOutputStream rulesResponse;
    @Shadow @Final private ServerInterface serverInterface;
    @Shadow private byte[] getIdentBytes(SocketAddress address) { throw new AssertionError(); }
    @Shadow private void sendTo(byte[] data, DatagramPacket packet) throws IOException { throw new AssertionError(); }
    // @formatter:on

    private GS4QueryEvent.QueryResponse fusioncore$query(GS4QueryEvent.QueryType type, DatagramPacket packet, boolean full) {
        GS4QueryEvent.QueryResponse.Builder builder = GS4QueryEvent.QueryResponse.builder()
            .motd(this.serverName)
            .map(this.worldName)
            .currentPlayers(this.serverInterface.getPlayerCount())
            .maxPlayers(this.maxPlayers)
            .port(this.serverPort)
            .hostname(this.hostIp)
            .gameVersion(this.serverInterface.getServerVersion())
            .serverVersion(Bukkit.getServer().getName() + " on " + Bukkit.getServer().getBukkitVersion());
        if (full) {
            List<GS4QueryEvent.QueryResponse.PluginInformation> plugins = Collections.emptyList();
            Plugin[] bukkitPlugins;
            if (((CraftServer) Bukkit.getServer()).getQueryPlugins() && (bukkitPlugins = Bukkit.getPluginManager().getPlugins()).length > 0) {
                plugins = Arrays.stream(bukkitPlugins)
                    .map(plugin -> GS4QueryEvent.QueryResponse.PluginInformation.of(plugin.getName(), plugin.getDescription().getVersion()))
                    .toList();
            }
            builder.plugins(plugins).players(this.serverInterface.getPlayerNames());
        }
        GS4QueryEvent event = new GS4QueryEvent(type, packet.getAddress(), builder.build());
        io.izzel.arclight.common.mod.util.FusionCoreEvents.call(event);
        return event.getResponse();
    }

    @Inject(method = "processPacket", cancellable = true, at = @At(value = "NEW", target = "net/minecraft/server/rcon/NetworkDataOutputStream"))
    private void fusioncore$basicStatus(DatagramPacket packet, CallbackInfoReturnable<Boolean> cir) throws IOException {
        GS4QueryEvent.QueryResponse response = this.fusioncore$query(GS4QueryEvent.QueryType.BASIC, packet, false);
        NetworkDataOutputStream out = new NetworkDataOutputStream(1460);
        out.write(0);
        out.writeBytes(this.getIdentBytes(packet.getSocketAddress()));
        out.writeString(response.getMotd());
        out.writeString("SMP");
        out.writeString(response.getMap());
        out.writeString(Integer.toString(response.getCurrentPlayers()));
        out.writeString(Integer.toString(response.getMaxPlayers()));
        out.writeShort((short) response.getPort());
        out.writeString(response.getHostname());
        this.sendTo(out.toByteArray(), packet);
        cir.setReturnValue(true);
    }

    @Inject(method = "buildRuleResponse", cancellable = true,
        at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/rcon/NetworkDataOutputStream;reset()V"))
    private void fusioncore$fullStatus(DatagramPacket packet, CallbackInfoReturnable<byte[]> cir) throws IOException {
        GS4QueryEvent.QueryResponse response = this.fusioncore$query(GS4QueryEvent.QueryType.FULL, packet, true);
        NetworkDataOutputStream out = this.rulesResponse;
        out.write(0);
        out.writeBytes(this.getIdentBytes(packet.getSocketAddress()));
        out.writeString("splitnum");
        out.write(128);
        out.write(0);
        out.writeString("hostname");
        out.writeString(response.getMotd());
        out.writeString("gametype");
        out.writeString("SMP");
        out.writeString("game_id");
        out.writeString("MINECRAFT");
        out.writeString("version");
        out.writeString(response.getGameVersion());
        out.writeString("plugins");
        StringBuilder plugins = new StringBuilder(response.getServerVersion());
        if (!response.getPlugins().isEmpty()) {
            plugins.append(": ");
            Iterator<GS4QueryEvent.QueryResponse.PluginInformation> iterator = response.getPlugins().iterator();
            while (iterator.hasNext()) {
                GS4QueryEvent.QueryResponse.PluginInformation info = iterator.next();
                plugins.append(info.getName());
                if (info.getVersion() != null) {
                    plugins.append(' ').append(info.getVersion().replace(";", ","));
                }
                if (iterator.hasNext()) {
                    plugins.append("; ");
                }
            }
        }
        out.writeString(plugins.toString());
        out.writeString("map");
        out.writeString(response.getMap());
        out.writeString("numplayers");
        out.writeString(Integer.toString(response.getCurrentPlayers()));
        out.writeString("maxplayers");
        out.writeString(Integer.toString(response.getMaxPlayers()));
        out.writeString("hostport");
        out.writeString(Integer.toString(response.getPort()));
        out.writeString("hostip");
        out.writeString(response.getHostname());
        out.write(0);
        out.write(1);
        out.writeString("player_");
        out.write(0);
        for (String player : response.getPlayers()) {
            out.writeString(player);
        }
        out.write(0);
        cir.setReturnValue(out.toByteArray());
    }
}
