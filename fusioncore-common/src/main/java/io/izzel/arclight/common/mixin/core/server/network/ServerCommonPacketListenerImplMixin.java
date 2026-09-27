package io.izzel.arclight.common.mixin.core.server.network;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.bridge.core.server.network.ServerCommonPacketListenerImplBridge;
import io.izzel.arclight.common.bridge.core.server.MinecraftServerBridge;
import io.izzel.arclight.common.mod.mixins.annotation.CreateConstructor;
import io.izzel.arclight.common.mod.mixins.annotation.ShadowConstructor;
import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.craftbukkit.v.util.CraftChatMessage;
import org.bukkit.craftbukkit.v.util.Waitable;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin implements ServerCommonPacketListenerImplBridge, PacketListener, CraftPlayer.TransferCookieConnection {

    // @formatter:off
    @Shadow @Final public Connection connection;
    @Shadow @Final protected MinecraftServer server;
    @Shadow public abstract void send(Packet<?> p_300558_);
    @Shadow protected abstract boolean isSingleplayerOwner();
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private boolean transferred;
    @Shadow public abstract void disconnect(DisconnectionDetails disconnectionDetails);
    @Shadow public abstract void onDisconnect(DisconnectionDetails disconnectionDetails);
    @Shadow public abstract void disconnect(Component component);
    // @formatter:on

    protected ServerPlayer player;
    protected CraftServer cserver;
    public boolean processedDisconnect;

    public CraftPlayer getCraftPlayer() {
        return (this.player == null) ? null : ((ServerPlayerBridge) this.player).bridge$getBukkitEntity();
    }

    @ShadowConstructor
    public abstract void arclight$this(MinecraftServer server, Connection connection, CommonListenerCookie cookie);

    @CreateConstructor
    public void arclight$constructor(MinecraftServer server, Connection connection, CommonListenerCookie cookie, ServerPlayer player) {
        arclight$this(server, connection, cookie);
        this.player = player;
        ((ServerPlayerBridge) player).bridge$setTransferCookieConnection(this);
        this.cserver = (CraftServer) Bukkit.getServer(); // TODO: Use MinecraftServerBridge.bridge$getServer()
    }

    @Override
    public CraftServer bridge$getCraftServer() {
        return cserver;
    }

    @Override
    public CraftPlayer bridge$getCraftPlayer() {
        return getCraftPlayer();
    }

    @Override
    public ServerPlayer bridge$getPlayer() {
        return player;
    }

    @Override
    public void bridge$setPlayer(ServerPlayer player) {
        this.player = player;
        ((ServerPlayerBridge) this.player).bridge$setTransferCookieConnection(this);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$init(MinecraftServer p_299469_, Connection p_300872_, CommonListenerCookie p_300277_, CallbackInfo ci) {
        this.cserver = ((CraftServer) Bukkit.getServer());
    }

    // Fusion Core: @ModifyExpressionValue instead of @ModifyConstant. A @ModifyConstant from another mod on
    // the same constant (Connectivity) made Mixin skip this one with a "@ModifyConstant conflict" WARN;
    // expression wrappers chain instead. At least 25 s as before, a longer timeout set by a mod is kept.
    @ModifyExpressionValue(method = "keepConnectionAlive", at = @At(value = "CONSTANT", args = "longValue=15000"))
    private long arclight$incrKeepaliveTimeout(long timeout) {
        return Math.max(timeout, 25000L);
    }

    @Override
    public boolean bridge$processedDisconnect() {
        return this.processedDisconnect;
    }

    public final boolean isDisconnected() {
        return !((ServerPlayerBridge) this.player).bridge$isJoining() && !this.connection.isConnected();
    }

    @Override
    public boolean bridge$isDisconnected() {
        return this.isDisconnected();
    }

    public void disconnect(String s) {
        this.disconnect(Component.literal(s));
    }

    @Override
    public void bridge$disconnect(String s) {
        disconnect(s);
    }

    @Decorate(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"))
    private void arclight$kickEvent(Connection instance, Packet<?> packet, PacketSendListener packetSendListener, DisconnectionDetails disconnectionDetails) throws Throwable {
        if (this.processedDisconnect) {
            DecorationOps.cancel().invoke();
            return;
        }
        if (!this.cserver.isPrimaryThread()) {
            Waitable<?> waitable = new Waitable<>() {
                @Override
                protected Object evaluate() {
                    disconnect(disconnectionDetails);
                    return null;
                }
            };

            ((MinecraftServerBridge) this.server).bridge$queuedProcess(waitable);

            try {
                waitable.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            DecorationOps.cancel().invoke();
            return;
        }
        String leaveMessage = ChatFormatting.YELLOW + this.player.getScoreboardName() + " left the game.";
        PlayerKickEvent event = new PlayerKickEvent(getCraftPlayer(), CraftChatMessage.fromComponent(disconnectionDetails.reason()), leaveMessage);
        if (this.cserver.getServer().isRunning()) {
            this.cserver.getPluginManager().callEvent(event);
        }
        if (event.isCancelled()) {
            DecorationOps.cancel().invoke();
            return;
        }
        ArclightCaptures.captureQuitMessage(event.getLeaveMessage());
        ((ServerPlayerBridge) this.player).bridge$setQuitReason(org.bukkit.event.player.PlayerQuitEvent$QuitReason.KICKED); // Fusion Core: Paper's quit reason
        Component textComponent = CraftChatMessage.fromString(event.getReason(), true)[0];
        Packet<?> newPacket = new ClientboundDisconnectPacket(textComponent);
        DecorationOps.callsite().invoke(instance, newPacket, packetSendListener);
        this.onDisconnect(disconnectionDetails);
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", cancellable = true, at = @At("HEAD"))
    private void arclight$updateCompassTarget(Packet<?> packetIn, PacketSendListener futureListeners, CallbackInfo ci) {
        if (packetIn == null || processedDisconnect) {
            ci.cancel();
            return;
        }
        if (packetIn instanceof ClientboundSetDefaultSpawnPositionPacket packet6) {
            ((ServerPlayerBridge) this.player).bridge$setCompassTarget(new Location(this.getCraftPlayer().getWorld(), packet6.pos.getX(), packet6.pos.getY(), packet6.pos.getZ()));
        }
    }

    // Fusion Core: Paper's Player#unlistPlayer (Paper patch 0862): player info sent to this player keeps the players it
    // unlisted out of its tab list, whoever sends it (join, showPlayer, plugins, mods)
    @org.spongepowered.asm.mixin.injection.ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> fusioncore$keepUnlisted(Packet<?> packet) {
        if (packet instanceof net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket info && this.player != null) {
            return io.izzel.arclight.common.mod.util.FusionCorePlayers.keepUnlisted(this.player, info);
        }
        return packet;
    }

    @Inject(method = "handleResourcePackResponse", at = @At("RETURN"))
    private void arclight$handleResourcePackStatus(ServerboundResourcePackPacket packetIn, CallbackInfo ci) {
        PlayerResourcePackStatusEvent.Status status = PlayerResourcePackStatusEvent.Status.values()[packetIn.action().ordinal()];
        io.izzel.arclight.common.mod.util.FusionCorePlayers.state(this.player).fusioncore$setResourcePackStatus(status); // Fusion Core: Paper patch 0066, last status
        io.izzel.arclight.common.mod.util.FusionCoreAudiences.packStatus(this.getCraftPlayer(), packetIn.id(), packetIn.action()); // Fusion Core: Adventure callbacks, before the event as in Paper
        this.cserver.getPluginManager().callEvent(new PlayerResourcePackStatusEvent(this.getCraftPlayer(), packetIn.id(), status));
    }

    // Fusion Core: Paper's Player#getClientBrandName (Paper patch 0392); the client sends its brand during configuration
    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void fusioncore$clientBrand(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (packet.payload() instanceof net.minecraft.network.protocol.common.custom.BrandPayload brand && this.player != null) {
            io.izzel.arclight.common.mod.util.FusionCorePlayers.state(this.player).fusioncore$setClientBrand(brand.brand());
        }
    }

    @Inject(method = "handleCookieResponse", cancellable = true, at = @At("HEAD"))
    private void arclight$handleCookie(ServerboundCookieResponsePacket serverboundCookieResponsePacket, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(serverboundCookieResponsePacket, (ServerCommonPacketListenerImpl) (Object) this, this.server);
        if (((CraftPlayer) this.player.bridge$getBukkitEntity()).handleCookieResponse(serverboundCookieResponsePacket)) {
            ci.cancel();
        }
    }

    // Plugin channel impl moved to PSI
    // @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    // private void arclight$customPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci)

    @Override
    public boolean isTransferred() {
        return this.transferred;
    }

    @Override
    public ConnectionProtocol getProtocol() {
        return this.protocol();
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        this.send(packet);
    }

    @Override
    public void kickPlayer(Component component) {
        disconnect(component);
    }
}
