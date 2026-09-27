package io.izzel.arclight.common.mod.util;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.bridge.core.server.network.ServerGamePacketListenerImplBridge;
import io.izzel.arclight.common.mixin.core.world.entity.player.PlayerGameProfileAccessor;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffectInstance;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerShowEntityEvent;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotConfig;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** Fusion Core: Paper's {@code PlayerProfile} API outside the profile class (patches 0136, 0165, 0176). */
public final class FusionCoreProfiles {

    private FusionCoreProfiles() {
    }

    /** Paper's {@code Util.PROFILE_EXECUTOR}: profile lookups stay off the world-generation threads. */
    public static final ExecutorService PROFILE_EXECUTOR = Executors.newFixedThreadPool(2, new java.util.concurrent.ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger();

        @Override
        public Thread newThread(Runnable run) {
            Thread thread = new Thread(run, "Profile Lookup Executor #" + this.count.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });

    /** Paper's {@code isProxyOnlineMode()}: online mode, or BungeeCord/Velocity forwarding. */
    public static boolean isProxyOnlineMode() {
        return Bukkit.getOnlineMode() || SpigotConfig.bungee || VelocitySupport.isEnabled();
    }

    /** Paper's {@code GameProfileCache#getProfileIfCached}. */
    public static @Nullable GameProfile getProfileIfCached(String name) {
        return FusionCorePaperServer.getProfileIfCached(name);
    }

    // ---- Server#createProfile*

    public static PlayerProfile createProfile(UUID uuid) {
        return createProfile(uuid, null);
    }

    public static PlayerProfile createProfile(String name) {
        return createProfile(null, name);
    }

    public static PlayerProfile createProfile(@Nullable UUID uuid, @Nullable String name) {
        Player player = uuid != null ? Bukkit.getPlayer(uuid) : (name != null ? Bukkit.getPlayerExact(name) : null);
        if (player != null) return new CraftPlayerProfile((CraftPlayer) player);

        return new CraftPlayerProfile(uuid, name);
    }

    public static PlayerProfile createProfileExact(@Nullable UUID uuid, @Nullable String name) {
        Player player = uuid != null ? Bukkit.getPlayer(uuid) : (name != null ? Bukkit.getPlayerExact(name) : null);
        if (player == null) {
            return new CraftPlayerProfile(uuid, name);
        }

        if (Objects.equals(uuid, player.getUniqueId()) && Objects.equals(name, player.getName())) {
            return new CraftPlayerProfile((CraftPlayer) player);
        }

        final CraftPlayerProfile profile = new CraftPlayerProfile(uuid, name);
        profile.getGameProfile().getProperties().putAll(((CraftPlayer) player).getHandle().getGameProfile().getProperties());
        return profile;
    }

    // ---- Paper's profile where Spigot has its own

    /**
     * Paper's {@code OfflinePlayer#getPlayerProfile()}, whose return type clashes with Spigot's method: a copy of the
     * online player's profile, or of the offline player's.
     */
    public static PlayerProfile playerProfile(OfflinePlayer player) {
        if (player instanceof CraftPlayer craftPlayer) {
            return new CraftPlayerProfile(craftPlayer).clone();
        }
        return fromSpigot(player.getPlayerProfile());
    }

    /** Paper's profile with the contents of a Bukkit one (Spigot's {@code CraftPlayerProfile} or Paper's). */
    public static @Nullable PlayerProfile fromSpigot(@Nullable org.bukkit.profile.PlayerProfile profile) {
        if (profile == null || profile instanceof PlayerProfile) {
            return (PlayerProfile) profile;
        }
        if (profile instanceof org.bukkit.craftbukkit.v.profile.CraftPlayerProfile spigot) {
            return new CraftPlayerProfile(spigot.buildResolvableProfile());
        }
        // another implementation: its textures cannot be copied (Spigot's textures only take Spigot's)
        return new CraftPlayerProfile(profile.getUniqueId(), profile.getName());
    }

    /**
     * Spigot's copy of Paper's profile, for CraftBukkit code that casts to Spigot's {@code CraftPlayerProfile}: skull
     * items (unset name and id stay unset, as in Paper's {@code buildResolvableProfile}).
     */
    public static org.bukkit.profile.PlayerProfile toSpigotForSkull(org.bukkit.profile.PlayerProfile profile) {
        if (profile instanceof CraftPlayerProfile paper) {
            return new org.bukkit.craftbukkit.v.profile.CraftPlayerProfile(paper.buildResolvableProfile());
        }
        return profile;
    }

    /** As {@link #toSpigotForSkull}, for the profile ban list, which Paper keys by {@code buildGameProfile()}. */
    public static org.bukkit.profile.PlayerProfile toSpigotForBan(org.bukkit.profile.PlayerProfile profile) {
        if (profile instanceof CraftPlayerProfile paper) {
            return new org.bukkit.craftbukkit.v.profile.CraftPlayerProfile(paper.buildGameProfile());
        }
        return profile;
    }

    // ---- Player#setPlayerProfile (Paper patch 0176)

    /** Paper's {@code CraftPlayer#setPlayerProfile}: re-adds the player for every viewer so the new name and skin show. */
    public static void setPlayerProfile(Player player, PlayerProfile profile) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer self = craftPlayer.getHandle();
        GameProfile gameProfile = CraftPlayerProfile.asAuthlibCopy(profile);
        if (!((ServerPlayerBridge) self).bridge$sentListPacket()) {
            ((PlayerGameProfileAccessor) self).fusioncore$setGameProfile(gameProfile);
            return;
        }
        List<ServerPlayer> players = ArclightServer.getMinecraftServer().getPlayerList().getPlayers();
        // First unregister the player for all players with the OLD game profile
        for (ServerPlayer viewer : players) {
            if (((ServerPlayerBridge) viewer).bridge$getBukkitEntity().canSee(craftPlayer)) {
                unregisterEntity(viewer, self);
            }
        }

        // Set the game profile here, we should have unregistered the entity via iterating all player entities above.
        ((PlayerGameProfileAccessor) self).fusioncore$setGameProfile(gameProfile);

        // Re-register the game profile for all players
        for (ServerPlayer viewer : players) {
            CraftPlayer bukkitViewer = ((ServerPlayerBridge) viewer).bridge$getBukkitEntity();
            if (bukkitViewer.canSee(craftPlayer)) {
                trackAndShowEntity(viewer, self, gameProfile.getId());
                Bukkit.getPluginManager().callEvent(new PlayerShowEntityEvent(bukkitViewer, craftPlayer));
            }
        }

        // Refresh misc player things AFTER sending game profile
        refreshPlayer(craftPlayer);
    }

    /** Paper's {@code CraftPlayer#unregisterEntity}: Spigot's {@code untrackAndHideEntity} without the event. */
    private static void unregisterEntity(ServerPlayer viewer, ServerPlayer other) {
        ChunkMap tracker = viewer.serverLevel().getChunkSource().chunkMap;
        ChunkMap.TrackedEntity entry = tracker.entityMap.get(other.getId());
        if (entry != null) {
            entry.removePlayer(viewer);
        }
        if (((ServerPlayerBridge) other).bridge$sentListPacket()) {
            viewer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(other.getUUID())));
        }
    }

    /** Paper's {@code CraftPlayer#trackAndShowEntity} with a UUID override, without the event. */
    private static void trackAndShowEntity(ServerPlayer viewer, ServerPlayer other, UUID uuidOverride) {
        ChunkMap tracker = viewer.serverLevel().getChunkSource().chunkMap;
        UUID original = other.getUUID();
        other.setUUID(uuidOverride);
        try {
            viewer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(other)));
        } finally {
            other.setUUID(original);
        }
        ChunkMap.TrackedEntity entry = tracker.entityMap.get(other.getId());
        if (entry != null && !entry.seenBy.contains(viewer.connection)) {
            entry.updatePlayer(viewer);
        }
    }

    /** Paper's {@code CraftPlayer#refreshPlayer}. */
    private static void refreshPlayer(CraftPlayer player) {
        ServerPlayer handle = player.getHandle();
        org.bukkit.Location loc = player.getLocation();

        // Respawn the player then update their position and selected slot
        ServerLevel level = handle.serverLevel();
        handle.connection.send(new ClientboundRespawnPacket(handle.createCommonSpawnInfo(level), ClientboundRespawnPacket.KEEP_ALL_DATA));
        handle.onUpdateAbilities();
        ((ServerGamePacketListenerImplBridge) handle.connection).bridge$teleport(loc); // no PlayerTeleportEvent, as Paper's internalTeleport
        PlayerList playerList = ArclightServer.getMinecraftServer().getPlayerList();
        playerList.sendPlayerPermissionLevel(handle);
        playerList.sendLevelInfo(handle, level);
        playerList.sendAllPlayerInfo(handle);

        // Resend their XP and effects because the respawn packet resets it
        handle.connection.send(new ClientboundSetExperiencePacket(handle.experienceProgress, handle.totalExperience, handle.experienceLevel));
        for (MobEffectInstance mobEffect : handle.getActiveEffects()) {
            handle.connection.send(new ClientboundUpdateMobEffectPacket(handle.getId(), mobEffect, false));
        }
    }

    // ---- Skull / SkullMeta (Paper patch 0165)

    public static @Nullable PlayerProfile getPlayerProfile(org.bukkit.block.Skull skull) {
        return fromSpigot(skull.getOwnerProfile());
    }

    public static void setPlayerProfile(org.bukkit.block.Skull skull, PlayerProfile profile) {
        Preconditions.checkNotNull(profile, "profile");
        skull.setOwnerProfile(profile);
    }

    public static @Nullable PlayerProfile getPlayerProfile(org.bukkit.inventory.meta.SkullMeta meta) {
        return fromSpigot(meta.getOwnerProfile());
    }

    public static void setPlayerProfile(org.bukkit.inventory.meta.SkullMeta meta, @Nullable PlayerProfile profile) {
        meta.setOwnerProfile(profile);
    }
}
