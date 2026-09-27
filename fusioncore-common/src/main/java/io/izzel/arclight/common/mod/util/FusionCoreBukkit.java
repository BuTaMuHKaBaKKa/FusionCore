package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.server.scheduler.PaperSchedulers;
import io.papermc.paper.math.Position;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

/**
 * Fusion Core: Paper's static methods on {@link Bukkit}. {@code PaperCallTransformer} redirects plugin calls here;
 * declare new ones exactly as in Paper.
 */
public final class FusionCoreBukkit {

    private FusionCoreBukkit() {
    }

    // Folia region threading API (Paper patch 0395)

    public static RegionScheduler getRegionScheduler() {
        return PaperSchedulers.REGION;
    }

    public static AsyncScheduler getAsyncScheduler() {
        return PaperSchedulers.ASYNC;
    }

    public static GlobalRegionScheduler getGlobalRegionScheduler() {
        return PaperSchedulers.GLOBAL;
    }

    // Mob Goal API (Paper patch 0353)

    public static com.destroystokyo.paper.entity.ai.MobGoals getMobGoals() {
        return io.izzel.arclight.common.mod.server.entity.ai.PaperMobGoals.INSTANCE;
    }

    // Custom potion mixes (Paper patch 0669)

    public static org.bukkit.potion.PotionBrewer getPotionBrewer() {
        return io.papermc.paper.potion.PaperPotionBrewer.INSTANCE;
    }

    // Datapack API (Paper patch 0529)

    public static io.papermc.paper.datapack.DatapackManager getDatapackManager() {
        return io.papermc.paper.datapack.PaperDatapackManager.INSTANCE;
    }

    public static boolean isOwnedByCurrentRegion(World world, Position position) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    public static boolean isOwnedByCurrentRegion(World world, Position position, int squareRadiusChunks) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    public static boolean isOwnedByCurrentRegion(Location location) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    public static boolean isOwnedByCurrentRegion(Location location, int squareRadiusChunks) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    public static boolean isOwnedByCurrentRegion(Block block) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    public static boolean isOwnedByCurrentRegion(World world, int chunkX, int chunkZ) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    public static boolean isOwnedByCurrentRegion(World world, int chunkX, int chunkZ, int squareRadiusChunks) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    public static boolean isOwnedByCurrentRegion(Entity entity) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    // Server build information (Paper patch 0015)

    public static String getMinecraftVersion() {
        return net.minecraft.SharedConstants.getCurrentVersion().getName();
    }

    public static String getVersionMessage() {
        final io.papermc.paper.ServerBuildInfo version = io.papermc.paper.ServerBuildInfo.buildInfo();
        return "This server is running " + Bukkit.getName() + " version " + version.asString(io.papermc.paper.ServerBuildInfo.StringRepresentation.VERSION_FULL) + " (Implementing API version " + Bukkit.getBukkitVersion() + ")";
    }

    // Server information and lookups (several Paper patches, see FusionCorePaperServer)

    public static java.io.File getPluginsFolder() {
        return FusionCorePaperServer.getPluginsFolder();
    }

    public static World getWorld(org.bukkit.NamespacedKey worldKey) {
        return FusionCorePaperServer.getWorld((net.kyori.adventure.key.Key) (Object) worldKey);
    }

    public static World getWorld(net.kyori.adventure.key.Key worldKey) {
        return FusionCorePaperServer.getWorld(worldKey);
    }

    public static org.bukkit.OfflinePlayer getOfflinePlayerIfCached(String name) {
        return FusionCorePaperServer.getOfflinePlayerIfCached(name);
    }

    public static double[] getTPS() {
        return FusionCorePaperServer.getTPS();
    }

    public static long[] getTickTimes() {
        return FusionCorePaperServer.getTickTimes();
    }

    public static double getAverageTickTime() {
        return Bukkit.getServer() == null ? 0D : FusionCorePaperServer.getAverageTickTime();
    }

    public static org.bukkit.command.CommandMap getCommandMap() {
        return ((org.bukkit.craftbukkit.v.CraftServer) Bukkit.getServer()).getCommandMap();
    }

    public static int getCurrentTick() {
        return FusionCorePaperServer.getCurrentTick();
    }

    public static boolean isStopping() {
        return FusionCorePaperServer.isStopping();
    }

    // Adventure (Paper patch 0006 "Adventure"): the same bodies as the Server defaults in ServerMixin

    public static net.kyori.adventure.text.Component motd() {
        return FusionCoreAdventure.fromLegacy(Bukkit.getServer().getMotd());
    }

    public static void motd(net.kyori.adventure.text.Component motd) {
        Bukkit.getServer().setMotd(FusionCoreAdventure.toLegacyRgb(motd));
    }

    public static net.kyori.adventure.text.Component shutdownMessage() {
        String message = Bukkit.getServer().getShutdownMessage();
        return message == null ? null : FusionCoreAdventure.fromLegacy(message);
    }

    public static net.kyori.adventure.text.Component permissionMessage() {
        return FusionCorePaperServer.permissionMessage();
    }

    public static org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder owner, org.bukkit.event.inventory.InventoryType type, net.kyori.adventure.text.Component title) {
        return Bukkit.createInventory(owner, type, FusionCoreAdventure.toLegacyRgb(title));
    }

    public static org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder owner, int size, net.kyori.adventure.text.Component title) {
        return Bukkit.createInventory(owner, size, FusionCoreAdventure.toLegacyRgb(title));
    }

    public static org.bukkit.inventory.Merchant createMerchant(net.kyori.adventure.text.Component title) {
        return Bukkit.createMerchant(title == null ? null : FusionCoreAdventure.toLegacyRgb(title));
    }

    public static com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID uuid) {
        return FusionCoreProfiles.createProfile(uuid);
    }

    public static com.destroystokyo.paper.profile.PlayerProfile createProfile(String name) {
        return FusionCoreProfiles.createProfile(name);
    }

    public static com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID uuid, String name) {
        return FusionCoreProfiles.createProfile(uuid, name);
    }

    public static com.destroystokyo.paper.profile.PlayerProfile createProfileExact(java.util.UUID uuid, String name) {
        return FusionCoreProfiles.createProfileExact(uuid, name);
    }

    /** Paper's typed ban list lookup (Paper patch 0931 "improve BanList types"). */
    public static <B extends org.bukkit.BanList<E>, E> B getBanList(io.papermc.paper.ban.BanListType<B> type) {
        return FusionCorePaperServer.getBanList(type);
    }

    // Fusion Core: Paper's server methods (bodies in FusionCorePaperServer)

    public static boolean addRecipe(org.bukkit.inventory.Recipe recipe, boolean resendRecipes) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.addRecipe(recipe, resendRecipes);
    }

    public static boolean removeRecipe(org.bukkit.NamespacedKey key, boolean resendRecipes) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.removeRecipe(key, resendRecipes);
    }

    public static void updateRecipes() {
        io.izzel.arclight.common.mod.util.FusionCorePaperServer.updateRecipes();
    }

    public static void updateResources() {
        io.izzel.arclight.common.mod.util.FusionCorePaperServer.updateResources();
    }

    public static org.bukkit.command.CommandSender createCommandSender(java.util.function.Consumer<? super net.kyori.adventure.text.Component> feedback) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.createCommandSender(feedback);
    }

    public static org.bukkit.inventory.ItemStack createExplorerMap(World world, Location location, org.bukkit.generator.structure.StructureType structureType, org.bukkit.map.MapCursor.Type mapIcon) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.createExplorerMap(world, location, structureType, mapIcon, 100, true);
    }

    public static org.bukkit.inventory.ItemStack createExplorerMap(World world, Location location, org.bukkit.generator.structure.StructureType structureType, org.bukkit.map.MapCursor.Type mapIcon, int radius, boolean findUnexplored) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.createExplorerMap(world, location, structureType, mapIcon, radius, findUnexplored);
    }

    public static String getPermissionMessage() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getPermissionMessage();
    }

    public static java.util.UUID getPlayerUniqueId(String playerName) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getPlayerUniqueId(playerName);
    }

    public static boolean isTickingWorlds() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.isTickingWorlds();
    }

    public static boolean reloadCommandAliases() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.reloadCommandAliases();
    }

    public static void reloadPermissions() {
        io.izzel.arclight.common.mod.util.FusionCorePaperServer.reloadPermissions();
    }

    public static boolean suggestPlayerNamesWhenNullTabCompletions() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.suggestPlayerNamesWhenNullTabCompletions();
    }

    @SuppressWarnings("deprecation")
    public static void broadcast(net.md_5.bungee.api.chat.BaseComponent component) {
        org.bukkit.Bukkit.getServer().spigot().broadcast(component);
    }

    @SuppressWarnings("deprecation")
    public static void broadcast(net.md_5.bungee.api.chat.BaseComponent... components) {
        org.bukkit.Bukkit.getServer().spigot().broadcast(components);
    }
}
