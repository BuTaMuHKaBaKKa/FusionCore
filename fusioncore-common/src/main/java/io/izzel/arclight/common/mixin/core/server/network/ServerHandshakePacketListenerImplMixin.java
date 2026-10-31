package io.izzel.arclight.common.mixin.core.server.network;

import com.google.gson.Gson;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UndashedUuid;
import io.izzel.arclight.common.bridge.core.network.ConnectionBridge;
import io.izzel.arclight.common.mod.util.VelocitySupport;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.spigotmc.SpigotConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.HashMap;

@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class ServerHandshakePacketListenerImplMixin {

    private static final Gson gson = new Gson();
    private static final java.util.regex.Pattern HOST_PATTERN = java.util.regex.Pattern.compile("[0-9a-f\\.:]{0,45}");
    private static final HashMap<InetAddress, Long> throttleTracker = new HashMap<>();
    private static int throttleCounter = 0;

    // @formatter:off
    @Shadow @Final private Connection connection;
    // @formatter:on

    // Don't inject at head to ensure executed after Forge check
    // See the corresponding mixin on Forge side
    @Inject(method = "handleIntention", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/handshake/ClientIntentionPacket;intention()Lnet/minecraft/network/protocol/handshake/ClientIntent;"))
    private void arclight$setHostName(ClientIntentionPacket packet, CallbackInfo ci) {
        // TODO
        ((ConnectionBridge) this.connection).bridge$setHostname(packet.hostName() + ":" + packet.port());
        // Fusion Core: for Paper's NetworkClient; host cut at the first NUL, as Paper does for forwarding markers
        String host = packet.hostName();
        int nul = host.indexOf('\0');
        ((ConnectionBridge) this.connection).bridge$setClientIntention(packet.protocolVersion(),
            java.net.InetSocketAddress.createUnresolved(nul < 0 ? host : host.substring(0, nul), packet.port()));
    }

    // Fusion Core: spigot.yml messages.outdated-client/outdated-server, as Spigot (CraftBukkit patch 0017): an older protocol than
    // the server's gets the client message, a newer one the server message; {0} is the server's game version
    @WrapOperation(method = "beginLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent fusioncore$outdatedMessage(String key, Object[] args, Operation<MutableComponent> original,
                                                        @Local(argsOnly = true) ClientIntentionPacket packet) {
        boolean outdatedClient = packet.protocolVersion() < SharedConstants.getCurrentVersion().getProtocolVersion();
        String message = outdatedClient ? SpigotConfig.outdatedClientMessage : SpigotConfig.outdatedServerMessage;
        return Component.literal(MessageFormat.format(message.replace("'", "''"), SharedConstants.getCurrentVersion().getName()));
    }

    @Inject(method = "beginLogin", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/Connection;setupOutboundProtocol(Lnet/minecraft/network/ProtocolInfo;)V"))
    private void arclight$throttler(ClientIntentionPacket packet, boolean bl, CallbackInfo ci) {
        try {
            long currentTime = System.currentTimeMillis();
            long connectionThrottle = Bukkit.getServer().getConnectionThrottle();
            InetAddress address = ((InetSocketAddress) this.connection.getRemoteAddress()).getAddress();
            synchronized (throttleTracker) {
                // Fusion Core: exempt any loopback address (127.0.0.0/8, ::1), not only the string "127.0.0.1"
                if (throttleTracker.containsKey(address) && !address.isLoopbackAddress() && currentTime - throttleTracker.get(address) < connectionThrottle) {
                    throttleTracker.put(address, currentTime);
                    var component = Component.literal("Connection throttled! Please wait before reconnecting.");
                    this.connection.send(new ClientboundLoginDisconnectPacket(component));
                    this.connection.disconnect(component);
                    ci.cancel();
                    return;
                }
                throttleTracker.put(address, currentTime);
                ++throttleCounter;
                if (throttleCounter > 200) {
                    throttleCounter = 0;
                    // Fusion Core: drop the entries older than the throttle; Spigot compares the timestamp itself with the interval,
                    // which is true for every entry, so the cleanup emptied the map and let a fresh reconnect through
                    throttleTracker.entrySet().removeIf(entry -> currentTime - entry.getValue() > connectionThrottle);
                }
            }
        } catch (Throwable t) {
            LogManager.getLogger().debug("Failed to check connection throttle", t);
        }
    }

    @Inject(method = "beginLogin", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"))
    private void arclight$proxySupport(ClientIntentionPacket packet, boolean bl, CallbackInfo ci) {
        // Fusion Core: Paper's PlayerHandshakeEvent (patch 0089): a plugin may take over the forwarded data (BungeeGuard and the
        // like) or refuse the connection; when it does, Spigot's BungeeCord parsing is skipped
        boolean handledByEvent = false;
        if (com.destroystokyo.paper.event.player.PlayerHandshakeEvent.getHandlerList().getRegisteredListeners().length != 0) {
            java.net.SocketAddress socketAddress = this.connection.getRemoteAddress();
            String hostnameOfRemote = socketAddress instanceof InetSocketAddress inet ? inet.getHostString() : InetAddress.getLoopbackAddress().getHostAddress();
            var event = new com.destroystokyo.paper.event.player.PlayerHandshakeEvent(packet.hostName(), hostnameOfRemote, !SpigotConfig.bungee);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                if (event.isFailed()) {
                    Component component = io.izzel.arclight.common.mod.util.FusionCoreAdventure.asVanilla(event.failMessage());
                    this.connection.send(new ClientboundLoginDisconnectPacket(component));
                    this.connection.disconnect(component);
                    ci.cancel();
                    return;
                }
                if (event.getServerHostname() != null) {
                    packet = new ClientIntentionPacket(packet.protocolVersion(), event.getServerHostname(), packet.port(), packet.intention());
                }
                if (event.getSocketAddressHostname() != null) {
                    this.connection.address = new InetSocketAddress(event.getSocketAddressHostname(), socketAddress instanceof InetSocketAddress inet ? inet.getPort() : 0);
                }
                ((ConnectionBridge) this.connection).bridge$setSpoofedUUID(event.getUniqueId());
                ((ConnectionBridge) this.connection).bridge$setSpoofedProfile(gson.fromJson(event.getPropertiesJson(), Property[].class));
                handledByEvent = true;
            }
        }
        if (!VelocitySupport.isEnabled()) {
            String[] split = packet.hostName().split("\00");
            if (!handledByEvent && SpigotConfig.bungee) {
                if ((split.length == 3 || split.length == 4) && (HOST_PATTERN.matcher(split[1]).matches())) {
                    ((ConnectionBridge) this.connection).bridge$setHostname(split[0]);
                    this.connection.address = new InetSocketAddress(split[1], ((InetSocketAddress) this.connection.getRemoteAddress()).getPort());
                    ((ConnectionBridge) this.connection).bridge$setSpoofedUUID(UndashedUuid.fromStringLenient(split[2]));
                } else {
                    var component = Component.literal("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                    this.connection.send(new ClientboundLoginDisconnectPacket(component));
                    this.connection.disconnect(component);
                    ci.cancel();
                    return;
                }
                if (split.length == 4) {
                    ((ConnectionBridge) this.connection).bridge$setSpoofedProfile(gson.fromJson(split[3], Property[].class));
                }
            } else if ((split.length == 3 || split.length == 4) && (HOST_PATTERN.matcher(split[1]).matches())) {
                Component component = Component.literal("Unknown data in login hostname, did you forget to enable BungeeCord in spigot.yml?");
                this.connection.send(new ClientboundLoginDisconnectPacket(component));
                this.connection.disconnect(component);
                ci.cancel();
            }
        }
    }
}
