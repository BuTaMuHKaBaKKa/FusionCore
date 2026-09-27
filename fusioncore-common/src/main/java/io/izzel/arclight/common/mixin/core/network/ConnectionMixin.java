package io.izzel.arclight.common.mixin.core.network;

import com.mojang.authlib.properties.Property;
import io.izzel.arclight.common.bridge.core.network.ConnectionBridge;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Connection.class)
public class ConnectionMixin implements ConnectionBridge {

    @Shadow public boolean disconnectionHandled;
    @Shadow @Nullable private volatile PacketListener packetListener;
    public java.util.UUID spoofedUUID;
    public com.mojang.authlib.properties.Property[] spoofedProfile;
    public String hostname;

    @Override
    public UUID bridge$getSpoofedUUID() {
        return spoofedUUID;
    }

    @Override
    public void bridge$setSpoofedUUID(UUID spoofedUUID) {
        this.spoofedUUID = spoofedUUID;
    }

    @Override
    public Property[] bridge$getSpoofedProfile() {
        return spoofedProfile;
    }

    @Override
    public void bridge$setSpoofedProfile(Property[] spoofedProfile) {
        this.spoofedProfile = spoofedProfile;
    }

    @Override
    public String bridge$getHostname() {
        return hostname;
    }

    @Override
    public void bridge$setHostname(String hostname) {
        this.hostname = hostname;
    }

    // Fusion Core: protocol version and virtual host from the handshake (Paper's NetworkClient)
    @org.spongepowered.asm.mixin.Unique private int fusioncore$protocolVersion = -1;
    @org.spongepowered.asm.mixin.Unique private java.net.InetSocketAddress fusioncore$virtualHost;

    @Override
    public int bridge$getProtocolVersion() {
        return this.fusioncore$protocolVersion;
    }

    @Override
    public java.net.InetSocketAddress bridge$getVirtualHost() {
        return this.fusioncore$virtualHost;
    }

    @Override
    public void bridge$setClientIntention(int protocolVersion, java.net.InetSocketAddress virtualHost) {
        this.fusioncore$protocolVersion = protocolVersion;
        this.fusioncore$virtualHost = virtualHost;
    }

    @Inject(method = "handleDisconnection", at = @At("HEAD"), cancellable = true)
    private void arclight$noDisconnectTwiceWarn(CallbackInfo ci) {
        if (disconnectionHandled) {
            ci.cancel();
        }
    }

    // Fusion Core: Paper's PlayerConnectionCloseEvent (patch 0270), after the listener has handled the disconnect (so after
    // PlayerQuitEvent), for players past authentication
    @Inject(method = "handleDisconnection", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/PacketListener;onDisconnect(Lnet/minecraft/network/DisconnectionDetails;)V"))
    private void fusioncore$connectionCloseEvent(CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCorePaperPlayerEvents.connectionClosed((Connection) (Object) this, this.packetListener);
    }

    // Fusion Core: port of Paper's "Fix sending disconnect packet in phases where it doesn't exist".
    // The handshake and status protocols have no disconnect packet, so vanilla fails with
    // "Sending unknown packet" when a server list ping errors out. Close the channel instead.
    @Redirect(method = "exceptionCaught", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;getSending()Lnet/minecraft/network/protocol/PacketFlow;"))
    private PacketFlow fusioncore$noDisconnectPacketBeforeLogin(Connection connection) {
        PacketListener listener = this.packetListener;
        if (listener != null && (listener.protocol() == ConnectionProtocol.STATUS || listener.protocol() == ConnectionProtocol.HANDSHAKING)) {
            return PacketFlow.SERVERBOUND;
        }
        return connection.getSending();
    }

    // Fusion Core: Paper's quit reason (Paper patch 0430): a timeout or an error in the connection of a player in game
    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    private void fusioncore$quitReason(io.netty.channel.ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
        if (!(throwable instanceof net.minecraft.network.SkipPacketException) && this.packetListener instanceof net.minecraft.server.network.ServerGamePacketListenerImpl game
            && ((io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge) game.player).bridge$getQuitReason() == null) {
            ((io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge) game.player).bridge$setQuitReason(throwable instanceof io.netty.handler.timeout.TimeoutException
                ? org.bukkit.event.player.PlayerQuitEvent$QuitReason.TIMED_OUT : org.bukkit.event.player.PlayerQuitEvent$QuitReason.ERRONEOUS_STATE);
        }
    }

    // Fusion Core: Paper's pipeline events for channel initialization listeners (patch 0521): ViaVersion and others follow compression
    @Shadow private io.netty.channel.Channel channel;

    @Inject(method = "setupCompression", at = @At("RETURN"))
    private void fusioncore$compressionEvent(int threshold, boolean validateDecompressed, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        this.channel.pipeline().fireUserEventTriggered(threshold >= 0
            ? io.papermc.paper.network.ConnectionEvent.COMPRESSION_THRESHOLD_SET
            : io.papermc.paper.network.ConnectionEvent.COMPRESSION_DISABLED);
    }
}
