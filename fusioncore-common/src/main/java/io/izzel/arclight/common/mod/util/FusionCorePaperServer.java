package io.izzel.arclight.common.mod.util;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import io.izzel.arclight.common.bridge.core.server.MinecraftServerBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.mixin.core.server.players.GameProfileCacheAccessor;
import io.izzel.arclight.common.mixin.core.server.players.GameProfileInfoAccessor;
import io.izzel.arclight.common.mod.ArclightConstants;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.kyori.adventure.key.Key;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.GameProfileCache;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v.CraftServer;

import java.io.File;
import java.util.Locale;

/** Fusion Core: bodies of Paper's server-wide API, shared by {@code ServerMixin} and {@link FusionCoreBukkit}. */
public final class FusionCorePaperServer {

    private FusionCorePaperServer() {
    }

    private static MinecraftServer server() {
        return ArclightServer.getMinecraftServer();
    }

    /** Paper: {@code CraftServer#getTPS}; Spigot's averages over 1, 5 and 15 minutes (its {@code /tps}). */
    public static double[] getTPS() {
        return ((MinecraftServerBridge) server()).bridge$getRecentTps().clone();
    }

    /** Paper patch 0325: the last 100 tick durations in nanoseconds; vanilla keeps the same ring buffer. */
    public static long[] getTickTimes() {
        return server().getTickTimesNanos().clone();
    }

    /** Paper patch 0325: mean of the last 100 tick durations, in milliseconds. */
    public static double getAverageTickTime() {
        long total = 0L;
        for (long value : server().getTickTimesNanos()) {
            total += value;
        }
        return ((double) total / 100) * 1.0E-6D;
    }

    /** Paper patch 0290: {@code MinecraftServer.currentTick}, wall-clock ticks since the epoch (Arclight keeps the same). */
    public static int getCurrentTick() {
        return ArclightConstants.currentTick;
    }

    /** Paper: {@code MinecraftServer#hasStopped}, set when shutdown starts. */
    public static boolean isStopping() {
        return ((MinecraftServerBridge) server()).bridge$hasStopped();
    }

    /** Paper: the {@code --plugins} directory. */
    public static File getPluginsFolder() {
        return ((MinecraftServerBridge) server()).bridge$getPluginsFolder();
    }

    /** Paper: {@code CraftServer#getWorld(Key)}, the world of that dimension key. */
    public static World getWorld(Key worldKey) {
        ServerLevel level = server().getLevel(ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(worldKey.namespace(), worldKey.value())));
        return level == null ? null : ((WorldBridge) level).bridge$getWorld();
    }

    /**
     * Paper patch 0424: an online player, or one in the profile cache, without a web request. (Paper
     * also drops an online player from CraftServer's offline-player cache here; that cache is private.)
     */
    public static OfflinePlayer getOfflinePlayerIfCached(String name) {
        Preconditions.checkArgument(name != null, "Name cannot be null");
        Preconditions.checkArgument(!name.isEmpty(), "Name cannot be empty");

        OfflinePlayer result = Bukkit.getPlayerExact(name);
        if (result == null) {
            GameProfile profile = getProfileIfCached(name);
            if (profile != null) {
                result = ((CraftServer) Bukkit.getServer()).getOfflinePlayer(profile);
            }
        }
        return result;
    }

    /** Paper's {@code GameProfileCache#getProfileIfCached}. */
    public static GameProfile getProfileIfCached(String name) {
        GameProfileCache cache = server().getProfileCache();
        if (cache == null) {
            return null;
        }
        Object entry = ((GameProfileCacheAccessor) cache).fusioncore$profilesByName().get(name.toLowerCase(Locale.ROOT));
        if (entry == null) {
            return null;
        }
        ((GameProfileInfoAccessor) entry).fusioncore$setLastAccess(((GameProfileCacheAccessor) cache).fusioncore$getNextOperation());
        return ((GameProfileInfoAccessor) entry).fusioncore$getProfile();
    }

    /**
     * Paper: the "no permission" message (paper-global.yml messages.no-permission). Fusion Core has no Paper
     * config; this is Paper's default, which is also the text Spigot's {@code Command#testPermission} sends.
     */
    public static net.kyori.adventure.text.Component permissionMessage() {
        return net.kyori.adventure.text.Component.text("I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.",
            net.kyori.adventure.text.format.NamedTextColor.RED);
    }

    /**
     * Paper's {@code Server.Spigot#getPaperConfig()}: the proxy forwarding keys, built from {@code fusioncore.conf} and spigot.yml.
     */
    public static org.bukkit.configuration.file.YamlConfiguration paperConfig() {
        org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
        boolean velocity = io.izzel.arclight.common.mod.util.VelocitySupport.isEnabled();
        config.set("proxies.velocity.enabled", velocity);
        config.set("proxies.velocity.online-mode", true);
        config.set("proxies.bungee-cord.online-mode", true);
        config.set("settings.velocity-support.enabled", velocity);
        config.set("settings.velocity-support.online-mode", true);
        config.set("settings.bungee-online-mode", true);
        return config;
    }

    /**
     * Paper's {@code getBanList(BanListType)} (Paper patch 0931 "improve BanList types"): the same lists Spigot hands out
     * for {@code BanList.Type.IP} and {@code BanList.Type.PROFILE}.
     */
    @SuppressWarnings("unchecked")
    public static <B extends org.bukkit.BanList<E>, E> B getBanList(io.papermc.paper.ban.BanListType<B> type) {
        com.google.common.base.Preconditions.checkArgument(type != null, "BanList.BanType cannot be null");
        if (type == io.papermc.paper.ban.BanListType.IP) {
            return (B) org.bukkit.Bukkit.getBanList(org.bukkit.BanList.Type.IP);
        } else if (type == io.papermc.paper.ban.BanListType.PROFILE) {
            return (B) org.bukkit.Bukkit.getBanList(org.bukkit.BanList.Type.PROFILE);
        }
        throw new IllegalArgumentException("Unknown BanListType: " + type);
    }

    // Fusion Core: the rest of Paper's Server methods

    /** Paper patch 0845: tags, advancements and recipes resent to every player, as {@code /reload} does. */
    public static void updateResources() {
        server().getPlayerList().reloadResources();
    }

    /** Paper patch 0845: the recipe list and each player's recipe book resent. */
    public static void updateRecipes() {
        net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket packet =
            new net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket(server().getRecipeManager().getOrderedRecipes());
        for (net.minecraft.server.level.ServerPlayer player : server().getPlayerList().getPlayers()) {
            player.connection.send(packet);
            player.getRecipeBook().sendInitialRecipeBook(player);
        }
    }

    /** Paper patch 0845: {@code addRecipe}, then the recipes resent when asked. */
    public static boolean addRecipe(org.bukkit.inventory.Recipe recipe, boolean resendRecipes) {
        boolean added = Bukkit.getServer().addRecipe(recipe);
        if (resendRecipes) {
            updateRecipes();
        }
        return added;
    }

    /** Paper patch 0845: {@code removeRecipe}, then the recipes resent when one was removed and it was asked. */
    public static boolean removeRecipe(org.bukkit.NamespacedKey key, boolean resendRecipes) {
        boolean removed = Bukkit.getServer().removeRecipe(key);
        if (removed && resendRecipes) {
            updateRecipes();
        }
        return removed;
    }

    /** Paper patch 0658: a sender with the console's rights whose command feedback goes to {@code feedback}. */
    public static org.bukkit.command.CommandSender createCommandSender(java.util.function.Consumer<? super net.kyori.adventure.text.Component> feedback) {
        return new FusionCoreFeedbackSender(feedback);
    }

    /** Paper patch 0857: an explorer map to the nearest structure of the (non-legacy) type, or null if none is found. */
    public static org.bukkit.inventory.ItemStack createExplorerMap(World world, org.bukkit.Location location, org.bukkit.generator.structure.StructureType structureType,
                                                                   org.bukkit.map.MapCursor.Type mapIcon, int radius, boolean findUnexplored) {
        Preconditions.checkArgument(world != null, "World cannot be null");
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(structureType != null, "StructureType cannot be null");
        Preconditions.checkArgument(mapIcon != null, "mapIcon cannot be null");
        ServerLevel level = ((org.bukkit.craftbukkit.v.CraftWorld) world).getHandle();
        org.bukkit.util.StructureSearchResult result = world.locateNearestStructure(location, structureType, radius, findUnexplored);
        if (result == null) {
            return null;
        }
        org.bukkit.Location found = result.getLocation();
        net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(found.getBlockX(), found.getBlockY(), found.getBlockZ());
        // Create map with showIcons = true, unlimitedTracking = true
        net.minecraft.world.item.ItemStack stack = net.minecraft.world.item.MapItem.create(level, pos.getX(), pos.getZ(), org.bukkit.map.MapView.Scale.NORMAL.getValue(), true, true);
        net.minecraft.world.item.MapItem.renderBiomePreviewMap(level, stack);
        // "+" map ID taken from VillagerTrades$TreasureMapForEmeralds
        net.minecraft.world.level.saveddata.maps.MapItemSavedData.addTargetDecoration(stack, pos, "+",
            org.bukkit.craftbukkit.v.map.CraftMapCursor.CraftType.bukkitToMinecraftHolder(mapIcon));
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asBukkitCopy(stack);
    }

    /** Paper patch 0266: the default no-permission message, legacy with {@code &} codes. */
    public static String getPermissionMessage() {
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().serialize(permissionMessage());
    }

    /**
     * Paper patch 0177: an online player's UUID; otherwise, when the server or its proxy authenticates players, the
     * cached profile's (null if unknown), else the offline-mode UUID for the name.
     */
    public static java.util.UUID getPlayerUniqueId(String name) {
        org.bukkit.entity.Player player = Bukkit.getPlayerExact(name);
        if (player != null) {
            return player.getUniqueId();
        }
        GameProfile profile;
        if (proxyOnlineMode()) {
            profile = server().getProfileCache() == null ? null : server().getProfileCache().get(name).orElse(null);
        } else {
            profile = new GameProfile(java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8)), name);
        }
        return profile != null ? profile.getId() : null;
    }

    /** Paper's {@code isProxyOnlineMode} with its defaults: forwarded players are online-mode players. */
    public static boolean proxyOnlineMode() {
        return Bukkit.getOnlineMode() || org.spigotmc.SpigotConfig.bungee || VelocitySupport.isEnabled();
    }

    private static volatile boolean tickingWorlds;

    /** Paper patch 0707: true while the server ticks its worlds (set around the world loop of {@code tickChildren}). */
    public static boolean isTickingWorlds() {
        return tickingWorlds;
    }

    public static void setTickingWorlds(boolean ticking) {
        tickingWorlds = ticking;
    }

    /** Paper patch 0130, Paper's default: player names are suggested when a plugin completer returns null. */
    public static boolean suggestPlayerNamesWhenNullTabCompletions() {
        return true;
    }

    public static boolean reloadCommandAliases() {
        return ((io.izzel.arclight.common.bridge.bukkit.CraftServerBridge) Bukkit.getServer()).fusioncore$reloadCommandAliases();
    }

    public static void reloadPermissions() {
        ((io.izzel.arclight.common.bridge.bukkit.CraftServerBridge) Bukkit.getServer()).fusioncore$reloadPermissions();
    }

    /** Paper: {@code Server.Spigot#getBukkitConfig}, the loaded bukkit.yml. */
    public static org.bukkit.configuration.file.YamlConfiguration bukkitConfig() {
        return ((io.izzel.arclight.common.bridge.bukkit.CraftServerBridge) Bukkit.getServer()).fusioncore$bukkitConfig();
    }

    /** Paper (patch 0965): the help map is rebuilt after a data pack reload, with the plugin commands registered again. */
    public static void reloadHelpMap() {
        if (Bukkit.getServer() == null) {
            return;
        }
        org.bukkit.craftbukkit.v.help.SimpleHelpMap helpMap = (org.bukkit.craftbukkit.v.help.SimpleHelpMap) Bukkit.getServer().getHelpMap();
        helpMap.clear();
        helpMap.initializeGeneralTopics();
        helpMap.initializeCommands();
    }
}
