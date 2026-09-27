package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.server.scheduler.PaperSchedulers;
import io.izzel.arclight.common.mod.util.FusionCorePaperCalls;
import io.papermc.paper.math.Position;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Entity;
import org.bukkit.craftbukkit.v.CraftServer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code Server#getCommandMap()}. This default runs because {@code CraftServer}'s method has a
 * narrower return type.
 */
@Mixin(value = Server.class, remap = false)
public interface ServerMixin extends net.kyori.adventure.audience.ForwardingAudience {

    default CommandMap getCommandMap() {
        return ((CraftServer) (Object) this).getCommandMap();
    }

    /** Fusion Core: Paper's component broadcast; {@code Bukkit.broadcast(Component)} lands in the same code. */
    default int broadcast(Component message) {
        return FusionCorePaperCalls.broadcast(message);
    }

    default int broadcast(Component message, String permission) {
        return FusionCorePaperCalls.broadcast(message, permission);
    }

    // Fusion Core: Paper's Folia scheduler and owned region API (patch 0843), see PaperSchedulers

    default RegionScheduler getRegionScheduler() {
        return PaperSchedulers.REGION;
    }

    default AsyncScheduler getAsyncScheduler() {
        return PaperSchedulers.ASYNC;
    }

    default GlobalRegionScheduler getGlobalRegionScheduler() {
        return PaperSchedulers.GLOBAL;
    }

    /** Fusion Core: Paper's Mob Goal API (patch 0353). */
    default com.destroystokyo.paper.entity.ai.MobGoals getMobGoals() {
        return io.izzel.arclight.common.mod.server.entity.ai.PaperMobGoals.INSTANCE;
    }

    /** Fusion Core: Paper's potion brewer with custom potion mixes (patch 0669). */
    default org.bukkit.potion.PotionBrewer getPotionBrewer() {
        return io.papermc.paper.potion.PaperPotionBrewer.INSTANCE;
    }

    /** Fusion Core: Paper's Datapack API (patch 0529). */
    default io.papermc.paper.datapack.DatapackManager getDatapackManager() {
        return io.papermc.paper.datapack.PaperDatapackManager.INSTANCE;
    }

    default boolean isOwnedByCurrentRegion(World world, Position position) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    default boolean isOwnedByCurrentRegion(World world, Position position, int squareRadiusChunks) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    default boolean isOwnedByCurrentRegion(Location location) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    default boolean isOwnedByCurrentRegion(Location location, int squareRadiusChunks) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    default boolean isOwnedByCurrentRegion(Block block) {
        return this.isOwnedByCurrentRegion(block.getLocation());
    }

    default boolean isOwnedByCurrentRegion(World world, int chunkX, int chunkZ) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    default boolean isOwnedByCurrentRegion(World world, int chunkX, int chunkZ, int squareRadiusChunks) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    default boolean isOwnedByCurrentRegion(Entity entity) {
        return PaperSchedulers.isOwnedByCurrentRegion();
    }

    /** Fusion Core: Paper's {@code getMinecraftVersion()} (patch 0015 "Expose server build information"). */
    default String getMinecraftVersion() {
        return net.minecraft.SharedConstants.getCurrentVersion().getName();
    }

    // Fusion Core: more of Paper's Server API, bodies in FusionCorePaperServer

    default java.io.File getPluginsFolder() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getPluginsFolder();
    }

    default World getWorld(org.bukkit.NamespacedKey worldKey) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getWorld((net.kyori.adventure.key.Key) (Object) worldKey);
    }

    default World getWorld(net.kyori.adventure.key.Key worldKey) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getWorld(worldKey);
    }

    default org.bukkit.OfflinePlayer getOfflinePlayerIfCached(String name) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getOfflinePlayerIfCached(name);
    }

    default double[] getTPS() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getTPS();
    }

    default long[] getTickTimes() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getTickTimes();
    }

    default double getAverageTickTime() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getAverageTickTime();
    }

    default int getCurrentTick() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getCurrentTick();
    }

    default boolean isStopping() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.isStopping();
    }

    // Fusion Core: Paper's Adventure API on Server (Paper patch 0006/0010 "Adventure")

    /** Paper: the server is an audience of the console and every online player. */
    @Override
    default Iterable<? extends net.kyori.adventure.audience.Audience> audiences() {
        Server self = (Server) this;
        return com.google.common.collect.Iterables.concat(java.util.Collections.singleton((net.kyori.adventure.audience.Audience) (Object) self.getConsoleSender()),
            (java.util.Collection<? extends net.kyori.adventure.audience.Audience>) (Object) self.getOnlinePlayers());
    }

    default net.kyori.adventure.text.Component motd() {
        return io.izzel.arclight.common.mod.util.FusionCoreAdventure.fromLegacy(((Server) this).getMotd());
    }

    default void motd(net.kyori.adventure.text.Component motd) {
        ((Server) this).setMotd(io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(motd));
    }

    default net.kyori.adventure.text.Component shutdownMessage() {
        String message = ((Server) this).getShutdownMessage();
        return message == null ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.fromLegacy(message);
    }

    default net.kyori.adventure.text.Component permissionMessage() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.permissionMessage();
    }

    default org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder owner, org.bukkit.event.inventory.InventoryType type, net.kyori.adventure.text.Component title) {
        return ((Server) this).createInventory(owner, type, io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(title));
    }

    default org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder owner, int size, net.kyori.adventure.text.Component title) {
        return ((Server) this).createInventory(owner, size, io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(title));
    }

    default org.bukkit.inventory.Merchant createMerchant(net.kyori.adventure.text.Component title) {
        return ((Server) this).createMerchant(title == null ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(title));
    }

    // Paper's PlayerProfile factories (Paper patch 0136), see FusionCoreProfiles

    default com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID uuid) {
        return io.izzel.arclight.common.mod.util.FusionCoreProfiles.createProfile(uuid);
    }

    default com.destroystokyo.paper.profile.PlayerProfile createProfile(String name) {
        return io.izzel.arclight.common.mod.util.FusionCoreProfiles.createProfile(name);
    }

    default com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID uuid, String name) {
        return io.izzel.arclight.common.mod.util.FusionCoreProfiles.createProfile(uuid, name);
    }

    default com.destroystokyo.paper.profile.PlayerProfile createProfileExact(java.util.UUID uuid, String name) {
        return io.izzel.arclight.common.mod.util.FusionCoreProfiles.createProfileExact(uuid, name);
    }

    default <B extends org.bukkit.BanList<E>, E> B getBanList(io.papermc.paper.ban.BanListType<B> type) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getBanList(type);
    }

    // Fusion Core: Paper's server methods (bodies in FusionCorePaperServer)

    default boolean addRecipe(org.bukkit.inventory.Recipe recipe, boolean resendRecipes) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.addRecipe(recipe, resendRecipes);
    }

    default boolean removeRecipe(org.bukkit.NamespacedKey key, boolean resendRecipes) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.removeRecipe(key, resendRecipes);
    }

    default void updateRecipes() {
        io.izzel.arclight.common.mod.util.FusionCorePaperServer.updateRecipes();
    }

    default void updateResources() {
        io.izzel.arclight.common.mod.util.FusionCorePaperServer.updateResources();
    }

    default org.bukkit.command.CommandSender createCommandSender(java.util.function.Consumer<? super net.kyori.adventure.text.Component> feedback) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.createCommandSender(feedback);
    }

    default org.bukkit.inventory.ItemStack createExplorerMap(World world, Location location, org.bukkit.generator.structure.StructureType structureType, org.bukkit.map.MapCursor.Type mapIcon) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.createExplorerMap(world, location, structureType, mapIcon, 100, true);
    }

    default org.bukkit.inventory.ItemStack createExplorerMap(World world, Location location, org.bukkit.generator.structure.StructureType structureType, org.bukkit.map.MapCursor.Type mapIcon, int radius, boolean findUnexplored) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.createExplorerMap(world, location, structureType, mapIcon, radius, findUnexplored);
    }

    default String getPermissionMessage() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getPermissionMessage();
    }

    default java.util.UUID getPlayerUniqueId(String playerName) {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.getPlayerUniqueId(playerName);
    }

    default boolean isTickingWorlds() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.isTickingWorlds();
    }

    default boolean reloadCommandAliases() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.reloadCommandAliases();
    }

    default void reloadPermissions() {
        io.izzel.arclight.common.mod.util.FusionCorePaperServer.reloadPermissions();
    }

    default boolean suggestPlayerNamesWhenNullTabCompletions() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.suggestPlayerNamesWhenNullTabCompletions();
    }

    @SuppressWarnings("deprecation")
    default void broadcast(net.md_5.bungee.api.chat.BaseComponent component) {
        ((Server) this).spigot().broadcast(component);
    }

    @SuppressWarnings("deprecation")
    default void broadcast(net.md_5.bungee.api.chat.BaseComponent... components) {
        ((Server) this).spigot().broadcast(components);
    }
}
