package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.bukkit.CraftPlayerTabListBridge;
import io.izzel.arclight.common.bridge.bukkit.MessengerBridge;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = CraftPlayer.class, remap = false)
public abstract class CraftPlayerMixin extends CraftEntityMixin implements CraftPlayerTabListBridge, io.izzel.arclight.common.bridge.bukkit.CraftPlayerPaperBridge {

    @Shadow @Final @Mutable private Set<String> channels;
    @Shadow private Component playerListHeader;
    @Shadow private Component playerListFooter;

    @Shadow private void updatePlayerListHeaderFooter() {}

    // Fusion Core: Paper's setPlayerListHeaderFooter(BaseComponent[], BaseComponent[]), see FusionCoreTitles
    @Override
    public void fusioncore$setPlayerListHeaderFooter(Component header, Component footer) {
        this.playerListHeader = header;
        this.playerListFooter = footer;
        this.updatePlayerListHeaderFooter();
    }

    @Override
    public Component fusioncore$getPlayerListHeader() {
        return this.playerListHeader;
    }

    @Override
    public Component fusioncore$getPlayerListFooter() {
        return this.playerListFooter;
    }

    @Shadow public abstract ServerPlayer getHandle();

    // Fusion Core start: Paper's listing API (Paper patch 0862); the info packets are fixed up in ServerCommonPacketListenerImplMixin
    private final Set<java.util.UUID> fusioncore$unlisted = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @Override
    public Set<java.util.UUID> fusioncore$unlisted() {
        return this.fusioncore$unlisted;
    }

    public boolean isListed(org.bukkit.entity.Player other) {
        return !this.fusioncore$unlisted.contains(other.getUniqueId());
    }

    public boolean unlistPlayer(org.bukkit.entity.Player other) {
        com.google.common.base.Preconditions.checkNotNull(other, "hidden entity cannot be null");
        if (this.getHandle().connection == null || !((org.bukkit.entity.Player) (Object) this).canSee(other)) {
            return false;
        }
        if (this.fusioncore$unlisted.add(other.getUniqueId())) {
            io.izzel.arclight.common.mod.util.FusionCorePlayers.sendListed((org.bukkit.entity.Player) (Object) this, other.getUniqueId(), false);
            return true;
        }
        return false;
    }

    public boolean listPlayer(org.bukkit.entity.Player other) {
        com.google.common.base.Preconditions.checkNotNull(other, "hidden entity cannot be null");
        if (this.getHandle().connection == null) {
            return false;
        }
        if (!((org.bukkit.entity.Player) (Object) this).canSee(other)) {
            throw new IllegalStateException("Player cannot see other player");
        }
        if (this.fusioncore$unlisted.remove(other.getUniqueId())) {
            io.izzel.arclight.common.mod.util.FusionCorePlayers.sendListed((org.bukkit.entity.Player) (Object) this, other.getUniqueId(), true);
            return true;
        }
        return false;
    }
    // Fusion Core end

    @Inject(method = "<init>", at = @At("TAIL"))
    private void arclight$useFastSet(CraftServer server, ServerPlayer entity, CallbackInfo ci) {
        channels = new ObjectOpenHashSet<>();
    }

    @ModifyConstant(method = "addChannel", constant = @Constant(intValue = 128))
    private int arclight$modifyMaxChannel(int constant) {
        return 2048;
    }
    
    /**
     * @author InitAuther97
     * @reason Use PSI and enhanced check.
     */
    @Overwrite
    private void sendCustomPayload(ResourceLocation location, byte[] data) {
        var messenger = (MessengerBridge) server.getMessenger();
        var craft = (CraftPlayer)(Object) this;
        messenger.arclight$sendCustomPayload(null, craft, location, data);
    }

    /**
     * @author InitAuther97
     * @reason Use PSI and enhanced check.
     */
    @Overwrite
    public void sendPluginMessage(Plugin source, String channel, byte[] data) {
        var messenger = (MessengerBridge) server.getMessenger();
        StandardMessenger.validatePluginMessage(server.getMessenger(), source, channel, data);
        if (this.getHandle().connection != null) {
            if (this.channels.contains(channel)) {
                ResourceLocation location = ResourceLocation.tryParse(StandardMessenger.validateAndCorrectChannel(channel));
                var craft = (CraftPlayer)(Object) this;
                messenger.arclight$sendCustomPayload(source, craft, location, data);
            }
        }
    }


    // Fusion Core: Paper's causes for kicks and game mode changes a plugin asks for (see FusionCoreEvents)
    @Inject(method = "kickPlayer", at = @At("HEAD"))
    private void fusioncore$kickCause(String message, CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCoreEvents.kickCause(org.bukkit.event.player.PlayerKickEvent$Cause.PLUGIN);
    }

    @Inject(method = "kickPlayer", at = @At("RETURN"))
    private void fusioncore$kickCauseDone(String message, CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCoreEvents.clearKickCause();
    }

    @Inject(method = "setGameMode", at = @At("HEAD"))
    private void fusioncore$gameModeCause(org.bukkit.GameMode mode, CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCoreEvents.gameModeCause(org.bukkit.event.player.PlayerGameModeChangeEvent$Cause.PLUGIN, null);
    }

    @Inject(method = "setGameMode", at = @At("RETURN"))
    private void fusioncore$gameModeCauseDone(org.bukkit.GameMode mode, CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCoreEvents.clearGameModeCause();
    }

    // Fusion Core start: Paper's teleport flags (Paper patch 0721), see FusionCoreTeleport
    @Inject(method = "teleport(Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)Z", cancellable = true, at = @At("HEAD"))
    private void fusioncore$playerTeleportFlags(org.bukkit.Location location, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause,
                                              org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir, @com.llamalad7.mixinextras.sugar.Share("fusioncore$flags") com.llamalad7.mixinextras.sugar.ref.LocalRef<java.util.Set<io.papermc.paper.entity.TeleportFlag>> flags) {
        flags.set(io.izzel.arclight.common.mod.util.FusionCoreTeleport.take());
        if (io.izzel.arclight.common.mod.util.FusionCoreTeleport.refused(this.getHandle(), this.getWorld(), location, flags.get())) {
            cir.setReturnValue(false);
        }
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "teleport(Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isVehicle()Z"))
    private boolean fusioncore$playerRetainPassengers(ServerPlayer handle, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original, @com.llamalad7.mixinextras.sugar.Share("fusioncore$flags") com.llamalad7.mixinextras.sugar.ref.LocalRef<java.util.Set<io.papermc.paper.entity.TeleportFlag>> flags) {
        return !flags.get().contains(io.papermc.paper.entity.TeleportFlag.EntityState.RETAIN_PASSENGERS) && original.call(handle);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "teleport(Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)Z", at = @At(value = "NEW", target = "org/bukkit/event/player/PlayerTeleportEvent"))
    private org.bukkit.event.player.PlayerTeleportEvent fusioncore$relativeInEvent(org.bukkit.entity.Player player, org.bukkit.Location from, org.bukkit.Location to,
                                                                                 org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause,
                                                                                 com.llamalad7.mixinextras.injector.wrapoperation.Operation<org.bukkit.event.player.PlayerTeleportEvent> original, @com.llamalad7.mixinextras.sugar.Share("fusioncore$flags") com.llamalad7.mixinextras.sugar.ref.LocalRef<java.util.Set<io.papermc.paper.entity.TeleportFlag>> flags) {
        return io.izzel.arclight.common.mod.util.FusionCoreTeleport.newEvent(player, from, to, cause,
            io.izzel.arclight.common.mod.util.FusionCoreTeleport.relativeOf(flags.get()));
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "teleport(Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;stopRiding()V"))
    private void fusioncore$playerRetainVehicle(ServerPlayer handle, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original, @com.llamalad7.mixinextras.sugar.Share("fusioncore$flags") com.llamalad7.mixinextras.sugar.ref.LocalRef<java.util.Set<io.papermc.paper.entity.TeleportFlag>> flags) {
        if (!flags.get().contains(io.papermc.paper.entity.TeleportFlag.EntityState.RETAIN_VEHICLE)) {
            original.call(handle);
        }
    }

    // Paper's close reason (Paper patch 0208) for the container closed by a teleport; RETAIN_OPEN_INVENTORY keeps it open
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "teleport(Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;closeContainer()V"))
    private void fusioncore$closeForTeleport(ServerPlayer handle, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original, @com.llamalad7.mixinextras.sugar.Share("fusioncore$flags") com.llamalad7.mixinextras.sugar.ref.LocalRef<java.util.Set<io.papermc.paper.entity.TeleportFlag>> flags) {
        if (!flags.get().contains(io.papermc.paper.entity.TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY)) {
            io.izzel.arclight.common.mod.util.FusionCoreEvents.closeWith(org.bukkit.event.inventory.InventoryCloseEvent$Reason.TELEPORT, () -> original.call(handle));
        }
    }

    // same-world teleport: with relative flags the client keeps its velocity on those axes (the event was already fired)
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "teleport(Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(Lorg/bukkit/Location;)V"))
    private void fusioncore$relativeTeleport(net.minecraft.server.network.ServerGamePacketListenerImpl connection, org.bukkit.Location to, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original, @com.llamalad7.mixinextras.sugar.Share("fusioncore$flags") com.llamalad7.mixinextras.sugar.ref.LocalRef<java.util.Set<io.papermc.paper.entity.TeleportFlag>> flags) {
        java.util.EnumSet<net.minecraft.world.entity.RelativeMovement> relative = io.izzel.arclight.common.mod.util.FusionCoreTeleport.toNms(flags.get());
        if (relative.isEmpty()) {
            original.call(connection, to);
        } else {
            ((io.izzel.arclight.common.bridge.core.server.network.ServerGamePacketListenerImplBridge) connection).fusioncore$teleport(to, relative);
        }
    }
    // Fusion Core end

    // Fusion Core: Paper's setRotation for players (Paper patch 0721): turns the player, Spigot throws
    @Inject(method = "setRotation", cancellable = true, at = @At("HEAD"))
    private void fusioncore$setRotation(float yaw, float pitch, CallbackInfo ci) {
        org.bukkit.Location target = ((org.bukkit.entity.Player) (Object) this).getEyeLocation();
        target.setYaw(yaw);
        target.setPitch(pitch);
        target.add(target.getDirection().multiply(9999999));
        this.getHandle().lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, new net.minecraft.world.phys.Vec3(target.getX(), target.getY(), target.getZ()));
        ci.cancel();
    }

    // Fusion Core start: state of Paper's Player API, see CraftPlayerPaperBridge
    private volatile boolean fusioncore$affectsSpawning = true;
    private volatile net.kyori.adventure.util.TriState fusioncore$flyingFallDamage = net.kyori.adventure.util.TriState.NOT_SET;
    private volatile String fusioncore$clientBrand;
    private volatile long fusioncore$loginTime;
    private volatile long fusioncore$lastSaveTime;
    private volatile org.bukkit.event.player.PlayerResourcePackStatusEvent.Status fusioncore$packStatus;
    private volatile int fusioncore$viewDistance = -1;
    private final it.unimi.dsi.fastutil.longs.LongOpenHashSet fusioncore$sentChunks = new it.unimi.dsi.fastutil.longs.LongOpenHashSet();

    @Override public boolean fusioncore$affectsSpawning() { return this.fusioncore$affectsSpawning; }
    @Override public void fusioncore$setAffectsSpawning(boolean affects) { this.fusioncore$affectsSpawning = affects; }
    @Override public net.kyori.adventure.util.TriState fusioncore$flyingFallDamage() { return this.fusioncore$flyingFallDamage; }
    @Override public void fusioncore$setFlyingFallDamage(net.kyori.adventure.util.TriState value) { this.fusioncore$flyingFallDamage = value; }
    @Override public String fusioncore$clientBrand() { return this.fusioncore$clientBrand; }
    @Override public void fusioncore$setClientBrand(String brand) { this.fusioncore$clientBrand = brand; }
    @Override public long fusioncore$loginTime() { return this.fusioncore$loginTime; }
    @Override public void fusioncore$setLoginTime(long loginTime) { this.fusioncore$loginTime = loginTime; }
    @Override public long fusioncore$lastSaveTime() { return this.fusioncore$lastSaveTime; }
    @Override public org.bukkit.event.player.PlayerResourcePackStatusEvent.Status fusioncore$resourcePackStatus() { return this.fusioncore$packStatus; }
    @Override public void fusioncore$setResourcePackStatus(org.bukkit.event.player.PlayerResourcePackStatusEvent.Status status) { this.fusioncore$packStatus = status; }
    @Override public int fusioncore$viewDistance() { return this.fusioncore$viewDistance; }
    @Override public void fusioncore$setViewDistance(int viewDistance) { this.fusioncore$viewDistance = viewDistance; }
    @Override public it.unimi.dsi.fastutil.longs.LongOpenHashSet fusioncore$sentChunks() { return this.fusioncore$sentChunks; }

    private final java.util.Set<net.kyori.adventure.bossbar.BossBar> fusioncore$bossBars = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final java.util.Map<java.util.UUID, net.kyori.adventure.resource.ResourcePackCallback> fusioncore$packCallbacks = new java.util.concurrent.ConcurrentHashMap<>();

    @Override public java.util.Set<net.kyori.adventure.bossbar.BossBar> fusioncore$bossBars() { return this.fusioncore$bossBars; }
    @Override public java.util.Map<java.util.UUID, net.kyori.adventure.resource.ResourcePackCallback> fusioncore$packCallbacks() { return this.fusioncore$packCallbacks; }

    // Paper patch 0271: last login and last seen go into the player file, for OfflinePlayer#getLastLogin/getLastSeen
    @Inject(method = "setExtraData", at = @At("RETURN"))
    private void fusioncore$saveLastSeen(net.minecraft.nbt.CompoundTag tag, CallbackInfo ci) {
        this.fusioncore$lastSaveTime = System.currentTimeMillis();
        if (!tag.contains("Paper")) {
            tag.put("Paper", new net.minecraft.nbt.CompoundTag());
        }
        net.minecraft.nbt.CompoundTag paper = tag.getCompound("Paper");
        paper.putLong("LastLogin", this.fusioncore$loginTime);
        paper.putLong("LastSeen", this.fusioncore$lastSaveTime);
    }
    // Fusion Core end
}
