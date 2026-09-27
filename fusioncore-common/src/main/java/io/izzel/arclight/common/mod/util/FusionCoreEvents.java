package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.bukkit.AsyncPlayerPreLoginEventBridge;
import io.izzel.arclight.common.bridge.bukkit.PlayerAdvancementDoneEventBridge;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent$Cause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerKickEvent$Cause;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.net.InetAddress;
import java.util.Set;
import java.util.UUID;

/**
 * Fusion Core: support for Paper's additions to Bukkit events: causes passed through thread-locals, and factories for
 * Paper's component constructors (used by {@code PaperCallTransformer}).
 */
public final class FusionCoreEvents {

    private FusionCoreEvents() {
    }

    /** Paper's {@code Event#callEvent} for core code (javac does not see what {@code EventMixin} adds): false when cancelled. */
    public static boolean call(org.bukkit.event.Event event) {
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        return !(event instanceof org.bukkit.event.Cancellable cancellable) || !cancellable.isCancelled();
    }

    // ---- causes

    private static final ThreadLocal<PlayerKickEvent$Cause> KICK_CAUSE = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.player.PlayerQuitEvent$QuitReason> QUIT_REASON = new ThreadLocal<>();

    public record GameModeChange(PlayerGameModeChangeEvent$Cause cause, Component cancelMessage) {}

    private static final ThreadLocal<GameModeChange> GAME_MODE_CHANGE = new ThreadLocal<>();

    /** The cause of the next kick on this thread, unless one is already set (the outermost caller knows best). */
    public static void kickCause(PlayerKickEvent$Cause cause) {
        if (KICK_CAUSE.get() == null) {
            KICK_CAUSE.set(cause);
        }
    }

    public static PlayerKickEvent$Cause takeKickCause() {
        PlayerKickEvent$Cause cause = KICK_CAUSE.get();
        KICK_CAUSE.remove();
        return cause == null ? PlayerKickEvent$Cause.UNKNOWN : cause;
    }

    public static void clearKickCause() {
        KICK_CAUSE.remove();
    }

    private static final ThreadLocal<org.bukkit.event.inventory.InventoryCloseEvent$Reason> CLOSE_REASON = new ThreadLocal<>();

    /**
     * Runs a container close with Paper's close reason. The outermost caller wins: a plugin's
     * {@code closeInventory()} inside a disconnect stays DISCONNECT, as in Paper, where the reason is passed down.
     */
    public static void closeWith(org.bukkit.event.inventory.InventoryCloseEvent$Reason reason, Runnable close) {
        if (CLOSE_REASON.get() != null) {
            close.run();
            return;
        }
        CLOSE_REASON.set(reason);
        try {
            close.run();
        } finally {
            CLOSE_REASON.remove();
        }
    }

    public static org.bukkit.event.inventory.InventoryCloseEvent$Reason closeReason() {
        org.bukkit.event.inventory.InventoryCloseEvent$Reason reason = CLOSE_REASON.get();
        return reason == null ? org.bukkit.event.inventory.InventoryCloseEvent$Reason.UNKNOWN : reason;
    }

    public static void gameModeCause(PlayerGameModeChangeEvent$Cause cause, Component cancelMessage) {
        if (GAME_MODE_CHANGE.get() == null) {
            GAME_MODE_CHANGE.set(new GameModeChange(cause, cancelMessage));
        }
    }

    public static GameModeChange takeGameModeChange() {
        GameModeChange change = GAME_MODE_CHANGE.get();
        GAME_MODE_CHANGE.remove();
        return change == null ? new GameModeChange(PlayerGameModeChangeEvent$Cause.UNKNOWN, null) : change;
    }

    public static void clearGameModeCause() {
        GAME_MODE_CHANGE.remove();
    }

    // ---- factories for Paper's component constructors (names are "new" + simple class name)

    private static String legacy(Component component) {
        return component == null ? null : FusionCoreAdventure.toLegacyRgb(component);
    }

    /** Paper's {@code PotionEffect} constructor with a hidden effect (Paper patch 0526). */
    public static org.bukkit.potion.PotionEffect newPotionEffect(org.bukkit.potion.PotionEffectType type, int duration, int amplifier, boolean ambient,
                                                                 boolean particles, boolean icon, org.bukkit.potion.PotionEffect hiddenEffect) {
        org.bukkit.potion.PotionEffect effect = new org.bukkit.potion.PotionEffect(type, duration, amplifier, ambient, particles, icon);
        ((io.izzel.arclight.common.bridge.bukkit.PotionEffectBridge) effect).bridge$setHiddenEffect(hiddenEffect);
        return effect;
    }

    /** Paper's constructor with whether the leash item drops. */
    public static org.bukkit.event.entity.EntityUnleashEvent newEntityUnleashEvent(org.bukkit.entity.Entity entity,
            org.bukkit.event.entity.EntityUnleashEvent.UnleashReason reason, boolean dropLeash) {
        var event = new org.bukkit.event.entity.EntityUnleashEvent(entity, reason);
        ((io.izzel.arclight.common.bridge.bukkit.EntityUnleashEventBridge) event).bridge$setDropLeash(dropLeash);
        return event;
    }

    /** Paper's constructor with whether the leash item drops. */
    public static org.bukkit.event.player.PlayerUnleashEntityEvent newPlayerUnleashEntityEvent(org.bukkit.entity.Entity entity, Player player,
            org.bukkit.inventory.EquipmentSlot hand, boolean dropLeash) {
        var event = new org.bukkit.event.player.PlayerUnleashEntityEvent(entity, player, hand);
        ((io.izzel.arclight.common.bridge.bukkit.EntityUnleashEventBridge) event).bridge$setDropLeash(dropLeash);
        return event;
    }

    /** Paper's constructor with the critical flag. */
    public static org.bukkit.event.entity.EntityDamageByEntityEvent newEntityDamageByEntityEvent(org.bukkit.entity.Entity damager, org.bukkit.entity.Entity damagee,
            org.bukkit.event.entity.EntityDamageEvent.DamageCause cause, org.bukkit.damage.DamageSource damageSource,
            java.util.Map<org.bukkit.event.entity.EntityDamageEvent.DamageModifier, Double> modifiers,
            java.util.Map<org.bukkit.event.entity.EntityDamageEvent.DamageModifier, ? extends com.google.common.base.Function<? super Double, Double>> modifierFunctions,
            boolean critical) {
        var event = new org.bukkit.event.entity.EntityDamageByEntityEvent(damager, damagee, cause, damageSource, modifiers, modifierFunctions);
        ((io.izzel.arclight.common.bridge.bukkit.EntityDamageByEntityEventBridge) event).bridge$setCritical(critical);
        return event;
    }

    // ---- Paper's constructors of server and vehicle events

    public static org.bukkit.event.server.TabCompleteEvent newTabCompleteEvent(CommandSender sender, String buffer, java.util.List<String> completions,
            boolean isCommand, org.bukkit.Location location) {
        var event = new org.bukkit.event.server.TabCompleteEvent(sender, buffer, completions);
        ((io.izzel.arclight.common.bridge.bukkit.TabCompleteEventBridge) event).bridge$init(isCommand, location);
        return event;
    }

    public static org.bukkit.event.vehicle.VehicleBlockCollisionEvent newVehicleBlockCollisionEvent(org.bukkit.entity.Vehicle vehicle, org.bukkit.block.Block block,
            org.bukkit.util.Vector velocity) {
        var event = new org.bukkit.event.vehicle.VehicleBlockCollisionEvent(vehicle, block);
        ((io.izzel.arclight.common.bridge.bukkit.VehicleBlockCollisionEventBridge) event).bridge$setVelocity(velocity);
        return event;
    }

    // ---- Paper's constructors of player events

    public static org.bukkit.event.player.PlayerItemDamageEvent newPlayerItemDamageEvent(Player player, org.bukkit.inventory.ItemStack what, int damage,
            int originalDamage) {
        var event = new org.bukkit.event.player.PlayerItemDamageEvent(player, what, damage);
        ((io.izzel.arclight.common.bridge.bukkit.PlayerEventExtrasBridge.ItemDamage) event).bridge$setOriginalDamage(originalDamage);
        return event;
    }

    public static org.bukkit.event.player.PlayerItemMendEvent newPlayerItemMendEvent(Player who, org.bukkit.inventory.ItemStack item,
            org.bukkit.inventory.EquipmentSlot slot, org.bukkit.entity.ExperienceOrb experienceOrb, int repairAmount, int consumedExperience) {
        var event = new org.bukkit.event.player.PlayerItemMendEvent(who, item, slot, experienceOrb, repairAmount);
        ((io.izzel.arclight.common.bridge.bukkit.PlayerEventExtrasBridge.ItemMend) event).bridge$setConsumedExperience(consumedExperience);
        return event;
    }

    public static org.bukkit.event.player.PlayerExpChangeEvent newPlayerExpChangeEvent(Player player, org.bukkit.entity.Entity sourceEntity, int expAmount) {
        var event = new org.bukkit.event.player.PlayerExpChangeEvent(player, expAmount);
        ((io.izzel.arclight.common.bridge.bukkit.PlayerEventExtrasBridge.ExpChange) event).bridge$setSource(sourceEntity);
        return event;
    }

    /** Paper's {@code CraftEventFactory#callPlayerItemMendEvent} with the consumed experience (patch 0815). */
    public static org.bukkit.event.player.PlayerItemMendEvent callPlayerItemMend(net.minecraft.world.entity.player.Player player,
            net.minecraft.world.entity.ExperienceOrb orb, net.minecraft.world.item.ItemStack item, net.minecraft.world.entity.EquipmentSlot slot,
            int repairAmount, int consumedExperience) {
        var event = newPlayerItemMendEvent((Player) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) player).bridge$getBukkitEntity(),
            org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(item), org.bukkit.craftbukkit.v.CraftEquipmentSlot.getSlot(slot),
            (org.bukkit.entity.ExperienceOrb) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) orb).bridge$getBukkitEntity(),
            repairAmount, consumedExperience);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    // ---- Paper's constructors of entity events

    public static org.bukkit.event.entity.EntityBreakDoorEvent newEntityBreakDoorEvent(org.bukkit.entity.LivingEntity entity, org.bukkit.block.Block targetBlock,
            org.bukkit.block.data.BlockData to) {
        var event = new org.bukkit.event.entity.EntityBreakDoorEvent(entity, targetBlock);
        ((io.izzel.arclight.common.bridge.bukkit.EntityEventExtrasBridge.ChangeBlock) event).bridge$setBlockData(to);
        return event;
    }

    public static org.bukkit.event.entity.EntityDismountEvent newEntityDismountEvent(org.bukkit.entity.Entity what, org.bukkit.entity.Entity dismounted,
            boolean isCancellable) {
        var event = new org.bukkit.event.entity.EntityDismountEvent(what, dismounted);
        ((io.izzel.arclight.common.bridge.bukkit.EntityEventExtrasBridge.Cancellability) event).bridge$setCancellable(isCancellable);
        return event;
    }

    public static org.bukkit.event.vehicle.VehicleExitEvent newVehicleExitEvent(org.bukkit.entity.Vehicle vehicle, org.bukkit.entity.LivingEntity exited,
            boolean isCancellable) {
        var event = new org.bukkit.event.vehicle.VehicleExitEvent(vehicle, exited);
        ((io.izzel.arclight.common.bridge.bukkit.EntityEventExtrasBridge.Cancellability) event).bridge$setCancellable(isCancellable);
        return event;
    }

    public static org.bukkit.event.entity.EntityPortalEnterEvent newEntityPortalEnterEvent(org.bukkit.entity.Entity entity, org.bukkit.Location location,
            org.bukkit.PortalType portalType) {
        var event = new org.bukkit.event.entity.EntityPortalEnterEvent(entity, location);
        ((io.izzel.arclight.common.bridge.bukkit.EntityEventExtrasBridge.Portal) event).bridge$setPortalType(portalType);
        return event;
    }

    public static org.bukkit.event.entity.EntityPortalEvent newEntityPortalEvent(org.bukkit.entity.Entity entity, org.bukkit.Location from, org.bukkit.Location to,
            int searchRadius, boolean canCreatePortal, int creationRadius, org.bukkit.PortalType portalType) {
        var event = new org.bukkit.event.entity.EntityPortalEvent(entity, from, to, searchRadius, canCreatePortal, creationRadius);
        ((io.izzel.arclight.common.bridge.bukkit.EntityEventExtrasBridge.Portal) event).bridge$setPortalType(portalType);
        return event;
    }

    public static org.bukkit.event.entity.EntityRegainHealthEvent newEntityRegainHealthEvent(org.bukkit.entity.Entity entity, double amount,
            org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason regainReason, boolean isFastRegen) {
        var event = new org.bukkit.event.entity.EntityRegainHealthEvent(entity, amount, regainReason);
        ((io.izzel.arclight.common.bridge.bukkit.EntityEventExtrasBridge.RegainHealth) event).bridge$setFastRegen(isFastRegen);
        return event;
    }

    public static org.bukkit.event.entity.EntityShootBowEvent newEntityShootBowEvent(org.bukkit.entity.LivingEntity shooter, org.bukkit.inventory.ItemStack bow,
            org.bukkit.entity.Entity projectile, float force) {
        return newEntityShootBowEvent(shooter, bow, new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR), projectile, force);
    }

    public static org.bukkit.event.entity.EntityShootBowEvent newEntityShootBowEvent(org.bukkit.entity.LivingEntity shooter, org.bukkit.inventory.ItemStack bow,
            org.bukkit.inventory.ItemStack arrowItem, org.bukkit.entity.Entity projectile, float force) {
        return new org.bukkit.event.entity.EntityShootBowEvent(shooter, bow, arrowItem, projectile, org.bukkit.inventory.EquipmentSlot.HAND, force, true);
    }

    /** Paper's constructors of {@code PlayerDeathEvent} with a component message and whether experience drops (patches 0006, 0176). */
    public static org.bukkit.event.entity.PlayerDeathEvent newPlayerDeathEvent(Player player, org.bukkit.damage.DamageSource damageSource,
            java.util.List<org.bukkit.inventory.ItemStack> drops, int droppedExp, Component deathMessage) {
        return newPlayerDeathEvent(player, damageSource, drops, droppedExp, 0, deathMessage);
    }

    public static org.bukkit.event.entity.PlayerDeathEvent newPlayerDeathEvent(Player player, org.bukkit.damage.DamageSource damageSource,
            java.util.List<org.bukkit.inventory.ItemStack> drops, int droppedExp, int newExp, Component deathMessage) {
        return newPlayerDeathEvent(player, damageSource, drops, droppedExp, newExp, 0, 0, deathMessage);
    }

    public static org.bukkit.event.entity.PlayerDeathEvent newPlayerDeathEvent(Player player, org.bukkit.damage.DamageSource damageSource,
            java.util.List<org.bukkit.inventory.ItemStack> drops, int droppedExp, int newExp, int newTotalExp, int newLevel, Component deathMessage) {
        return newPlayerDeathEvent(player, damageSource, drops, droppedExp, newExp, newTotalExp, newLevel, deathMessage, true);
    }

    public static org.bukkit.event.entity.PlayerDeathEvent newPlayerDeathEvent(Player player, org.bukkit.damage.DamageSource damageSource,
            java.util.List<org.bukkit.inventory.ItemStack> drops, int droppedExp, int newExp, int newTotalExp, int newLevel, Component deathMessage,
            boolean doExpDrop) {
        return newPlayerDeathEvent(player, damageSource, drops, droppedExp, newExp, newTotalExp, newLevel, deathMessage == null ? null : legacy(deathMessage), doExpDrop);
    }

    public static org.bukkit.event.entity.PlayerDeathEvent newPlayerDeathEvent(Player player, org.bukkit.damage.DamageSource damageSource,
            java.util.List<org.bukkit.inventory.ItemStack> drops, int droppedExp, int newExp, int newTotalExp, int newLevel, String deathMessage,
            boolean doExpDrop) {
        var event = new org.bukkit.event.entity.PlayerDeathEvent(player, damageSource, drops, droppedExp, newExp, newTotalExp, newLevel, deathMessage);
        ((io.izzel.arclight.common.bridge.bukkit.EntityDeathEventBridge) event).bridge$setShouldDropExperience(doExpDrop);
        return event;
    }

    public static BroadcastMessageEvent newBroadcastMessageEvent(Component message, Set<CommandSender> recipients) {
        return new BroadcastMessageEvent(legacy(message), recipients);
    }

    public static BroadcastMessageEvent newBroadcastMessageEvent(boolean isAsync, Component message, Set<CommandSender> recipients) {
        return new BroadcastMessageEvent(isAsync, legacy(message), recipients);
    }

    public static PlayerKickEvent newPlayerKickEvent(Player playerKicked, Component kickReason, Component leaveMessage) {
        return newPlayerKickEvent(playerKicked, kickReason, leaveMessage, PlayerKickEvent$Cause.UNKNOWN);
    }

    public static PlayerKickEvent newPlayerKickEvent(Player playerKicked, Component kickReason, Component leaveMessage, PlayerKickEvent$Cause cause) {
        clearKickCause();
        kickCause(cause);
        return new PlayerKickEvent(playerKicked, legacy(kickReason), legacy(leaveMessage));
    }

    public static PlayerJoinEvent newPlayerJoinEvent(Player playerJoined, Component joinMessage) {
        return new PlayerJoinEvent(playerJoined, legacy(joinMessage));
    }

    public static PlayerQuitEvent newPlayerQuitEvent(Player who, Component quitMessage) {
        return new PlayerQuitEvent(who, legacy(quitMessage));
    }

    public static PlayerQuitEvent newPlayerQuitEvent(Player who, Component quitMessage, org.bukkit.event.player.PlayerQuitEvent$QuitReason quitReason) {
        quitReason(quitReason);
        return new PlayerQuitEvent(who, legacy(quitMessage));
    }

    public static PlayerQuitEvent newPlayerQuitEvent(Player who, String quitMessage, org.bukkit.event.player.PlayerQuitEvent$QuitReason quitReason) {
        quitReason(quitReason);
        return new PlayerQuitEvent(who, quitMessage);
    }

    /** Paper's internal constructor with relative teleport flags (Paper patch 0353). */
    public static org.bukkit.event.player.PlayerTeleportEvent newPlayerTeleportEvent(Player player, org.bukkit.Location from, org.bukkit.Location to,
                                                                                     org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause,
                                                                                     java.util.Set<io.papermc.paper.entity.TeleportFlag.Relative> teleportFlagSet) {
        return FusionCoreTeleport.newEvent(player, from, to, cause, teleportFlagSet);
    }

    /** Paper's quit reason for the next PlayerQuitEvent built on this thread (Paper patch 0430). */
    public static void quitReason(org.bukkit.event.player.PlayerQuitEvent$QuitReason reason) {
        QUIT_REASON.set(reason);
    }

    public static org.bukkit.event.player.PlayerQuitEvent$QuitReason takeQuitReason() {
        org.bukkit.event.player.PlayerQuitEvent$QuitReason reason = QUIT_REASON.get();
        QUIT_REASON.remove();
        return reason == null ? org.bukkit.event.player.PlayerQuitEvent$QuitReason.DISCONNECTED : reason;
    }

    private static final ThreadLocal<com.google.common.collect.ImmutableSet.Builder<org.bukkit.event.player.PlayerRespawnEvent$RespawnFlag>> RESPAWN_FLAGS = new ThreadLocal<>();

    /** Paper's constructor with respawn flags (Paper patch 0519); bed and anchor flags are added by the event itself. */
    public static org.bukkit.event.player.PlayerRespawnEvent newPlayerRespawnEvent(Player respawnPlayer, org.bukkit.Location respawnLocation, boolean isBedSpawn,
                                                                               boolean isAnchorSpawn, org.bukkit.event.player.PlayerRespawnEvent.RespawnReason respawnReason,
                                                                               com.google.common.collect.ImmutableSet.Builder<org.bukkit.event.player.PlayerRespawnEvent$RespawnFlag> respawnFlags) {
        RESPAWN_FLAGS.set(respawnFlags);
        try {
            return new org.bukkit.event.player.PlayerRespawnEvent(respawnPlayer, respawnLocation, isBedSpawn, isAnchorSpawn, respawnReason);
        } finally {
            RESPAWN_FLAGS.remove();
        }
    }

    public static com.google.common.collect.ImmutableSet.Builder<org.bukkit.event.player.PlayerRespawnEvent$RespawnFlag> takeRespawnFlags() {
        var builder = RESPAWN_FLAGS.get();
        RESPAWN_FLAGS.remove();
        return builder == null ? com.google.common.collect.ImmutableSet.builder() : builder;
    }

    public static PlayerAdvancementDoneEvent newPlayerAdvancementDoneEvent(Player who, Advancement advancement, Component message) {
        PlayerAdvancementDoneEvent event = new PlayerAdvancementDoneEvent(who, advancement);
        ((PlayerAdvancementDoneEventBridge) event).fusioncore$setMessage(message);
        return event;
    }

    public static PlayerGameModeChangeEvent newPlayerGameModeChangeEvent(Player player, GameMode newGameMode, PlayerGameModeChangeEvent$Cause cause, Component cancelMessage) {
        clearGameModeCause();
        gameModeCause(cause, cancelMessage);
        return new PlayerGameModeChangeEvent(player, newGameMode);
    }

    public static PlayerLoginEvent newPlayerLoginEvent(Player player, String hostname, InetAddress address, PlayerLoginEvent.Result result, Component message, InetAddress realAddress) {
        return new PlayerLoginEvent(player, hostname, address, result, legacy(message), realAddress);
    }

    public static ServerListPingEvent newServerListPingEvent(String hostname, InetAddress address, Component motd, int numPlayers, int maxPlayers) {
        return new ServerListPingEvent(hostname, address, legacy(motd), numPlayers, maxPlayers);
    }

    public static ServerListPingEvent newServerListPingEvent(InetAddress address, Component motd, int numPlayers, int maxPlayers) {
        return new ServerListPingEvent("", address, legacy(motd), numPlayers, maxPlayers);
    }

    // Paper's constructors of AsyncPlayerPreLoginEvent with a profile (not components, but built the same way)

    public static AsyncPlayerPreLoginEvent newAsyncPlayerPreLoginEvent(String name, InetAddress ipAddress, UUID uniqueId, boolean transferred,
                                                                       com.destroystokyo.paper.profile.PlayerProfile profile) {
        return newAsyncPlayerPreLoginEvent(name, ipAddress, ipAddress, uniqueId, transferred, profile);
    }

    public static AsyncPlayerPreLoginEvent newAsyncPlayerPreLoginEvent(String name, InetAddress ipAddress, InetAddress rawAddress, UUID uniqueId,
                                                                       boolean transferred, com.destroystokyo.paper.profile.PlayerProfile profile) {
        return newAsyncPlayerPreLoginEvent(name, ipAddress, rawAddress, uniqueId, transferred, profile, "");
    }

    public static AsyncPlayerPreLoginEvent newAsyncPlayerPreLoginEvent(String name, InetAddress ipAddress, InetAddress rawAddress, UUID uniqueId,
                                                                       boolean transferred, com.destroystokyo.paper.profile.PlayerProfile profile, String hostname) {
        AsyncPlayerPreLoginEvent event = new AsyncPlayerPreLoginEvent(name, ipAddress, uniqueId, transferred);
        ((AsyncPlayerPreLoginEventBridge) event).fusioncore$setPaperFields(profile, rawAddress, hostname);
        return event;
    }

    public static org.bukkit.event.inventory.InventoryCloseEvent newInventoryCloseEvent(org.bukkit.inventory.InventoryView transaction,
                                                                                        org.bukkit.event.inventory.InventoryCloseEvent$Reason reason) {
        org.bukkit.event.inventory.InventoryCloseEvent event = new org.bukkit.event.inventory.InventoryCloseEvent(transaction);
        ((io.izzel.arclight.common.bridge.bukkit.InventoryCloseEventBridge) event).fusioncore$setReason(reason);
        return event;
    }

    // Paper's ServerResourcesReloadedEvent cause: COMMAND for /reload, otherwise PLUGIN (Bukkit's reloadData and others)
    private static final ThreadLocal<io.papermc.paper.event.server.ServerResourcesReloadedEvent.Cause> RELOAD_CAUSE = new ThreadLocal<>();

    public static void reloadCause(io.papermc.paper.event.server.ServerResourcesReloadedEvent.Cause cause) {
        RELOAD_CAUSE.set(cause);
    }

    public static io.papermc.paper.event.server.ServerResourcesReloadedEvent.Cause takeReloadCause() {
        io.papermc.paper.event.server.ServerResourcesReloadedEvent.Cause cause = RELOAD_CAUSE.get();
        RELOAD_CAUSE.remove();
        return cause == null ? io.papermc.paper.event.server.ServerResourcesReloadedEvent.Cause.PLUGIN : cause;
    }

    // Paper's weather change causes (patch 0540). Whoever calls setRaining/setThundering on the level data names the cause for
    // the duration of the call (ServerLevelMixin, CraftWorldMixin); PrimaryLevelDataMixin reads it and builds the event with the
    // factories below, which hand it to the event's constructor (WeatherChangeEventMixin, ThunderChangeEventMixin).
    private static final ThreadLocal<org.bukkit.event.weather.WeatherChangeEvent$Cause> RAIN_CAUSE = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.weather.ThunderChangeEvent$Cause> THUNDER_CAUSE = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.weather.WeatherChangeEvent$Cause> RAIN_EVENT_CAUSE = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.weather.ThunderChangeEvent$Cause> THUNDER_EVENT_CAUSE = new ThreadLocal<>();

    public static void withRainCause(org.bukkit.event.weather.WeatherChangeEvent$Cause cause, Runnable change) {
        org.bukkit.event.weather.WeatherChangeEvent$Cause outer = RAIN_CAUSE.get();
        RAIN_CAUSE.set(cause);
        try {
            change.run();
        } finally {
            if (outer == null) RAIN_CAUSE.remove(); else RAIN_CAUSE.set(outer);
        }
    }

    public static void withThunderCause(org.bukkit.event.weather.ThunderChangeEvent$Cause cause, Runnable change) {
        org.bukkit.event.weather.ThunderChangeEvent$Cause outer = THUNDER_CAUSE.get();
        THUNDER_CAUSE.set(cause);
        try {
            change.run();
        } finally {
            if (outer == null) THUNDER_CAUSE.remove(); else THUNDER_CAUSE.set(outer);
        }
    }

    public static org.bukkit.event.weather.WeatherChangeEvent$Cause rainCause() {
        org.bukkit.event.weather.WeatherChangeEvent$Cause cause = RAIN_CAUSE.get();
        return cause == null ? org.bukkit.event.weather.WeatherChangeEvent$Cause.UNKNOWN : cause;
    }

    public static org.bukkit.event.weather.ThunderChangeEvent$Cause thunderCause() {
        org.bukkit.event.weather.ThunderChangeEvent$Cause cause = THUNDER_CAUSE.get();
        return cause == null ? org.bukkit.event.weather.ThunderChangeEvent$Cause.UNKNOWN : cause;
    }

    public static org.bukkit.event.weather.WeatherChangeEvent newWeatherChangeEvent(org.bukkit.World world, boolean to,
                                                                                    org.bukkit.event.weather.WeatherChangeEvent$Cause cause) {
        RAIN_EVENT_CAUSE.set(cause);
        try {
            return new org.bukkit.event.weather.WeatherChangeEvent(world, to);
        } finally {
            RAIN_EVENT_CAUSE.remove();
        }
    }

    public static org.bukkit.event.weather.ThunderChangeEvent newThunderChangeEvent(org.bukkit.World world, boolean to,
                                                                                    org.bukkit.event.weather.ThunderChangeEvent$Cause cause) {
        THUNDER_EVENT_CAUSE.set(cause);
        try {
            return new org.bukkit.event.weather.ThunderChangeEvent(world, to);
        } finally {
            THUNDER_EVENT_CAUSE.remove();
        }
    }

    public static org.bukkit.event.weather.WeatherChangeEvent$Cause takeWeatherEventCause() {
        org.bukkit.event.weather.WeatherChangeEvent$Cause cause = RAIN_EVENT_CAUSE.get();
        RAIN_EVENT_CAUSE.remove();
        return cause == null ? org.bukkit.event.weather.WeatherChangeEvent$Cause.UNKNOWN : cause;
    }

    public static org.bukkit.event.weather.ThunderChangeEvent$Cause takeThunderEventCause() {
        org.bukkit.event.weather.ThunderChangeEvent$Cause cause = THUNDER_EVENT_CAUSE.get();
        THUNDER_EVENT_CAUSE.remove();
        return cause == null ? org.bukkit.event.weather.ThunderChangeEvent$Cause.UNKNOWN : cause;
    }

    // Paper's extra constructor argument of a Bukkit event (hand, block face, drops): the factory leaves it here while it
    // runs Spigot's constructor, the event's mixin takes it at the end of that constructor. Nothing left means Paper's default.
    private static final ThreadLocal<Object> CONSTRUCTOR_ARGUMENT = new ThreadLocal<>();

    private static <T> T withConstructorArgument(Object argument, java.util.function.Supplier<T> constructor) {
        CONSTRUCTOR_ARGUMENT.set(argument);
        try {
            return constructor.get();
        } finally {
            CONSTRUCTOR_ARGUMENT.remove();
        }
    }

    public static <T> T takeConstructorArgument(Class<T> type) {
        Object argument = CONSTRUCTOR_ARGUMENT.get();
        if (!type.isInstance(argument)) {
            return null;
        }
        CONSTRUCTOR_ARGUMENT.remove();
        return type.cast(argument);
    }

    public static org.bukkit.event.block.BlockCanBuildEvent newBlockCanBuildEvent(org.bukkit.block.Block block, Player player,
                                                                                  org.bukkit.block.data.BlockData type, boolean canBuild,
                                                                                  org.bukkit.inventory.EquipmentSlot hand) {
        return withConstructorArgument(hand, () -> new org.bukkit.event.block.BlockCanBuildEvent(block, player, type, canBuild));
    }

    public static org.bukkit.event.block.BlockDamageEvent newBlockDamageEvent(Player player, org.bukkit.block.Block block,
                                                                              org.bukkit.block.BlockFace blockFace,
                                                                              org.bukkit.inventory.ItemStack itemInHand, boolean instaBreak) {
        return withConstructorArgument(blockFace, () -> new org.bukkit.event.block.BlockDamageEvent(player, block, itemInHand, instaBreak));
    }

    public static org.bukkit.event.block.BlockMultiPlaceEvent newBlockMultiPlaceEvent(java.util.List<org.bukkit.block.BlockState> states,
                                                                                      org.bukkit.block.Block clicked,
                                                                                      org.bukkit.inventory.ItemStack itemInHand, Player thePlayer,
                                                                                      boolean canBuild, org.bukkit.inventory.EquipmentSlot hand) {
        return withConstructorArgument(hand, () -> new org.bukkit.event.block.BlockMultiPlaceEvent(states, clicked, itemInHand, thePlayer, canBuild));
    }

    public static org.bukkit.event.block.BlockPhysicsEvent newBlockPhysicsEvent(org.bukkit.block.Block block, org.bukkit.block.data.BlockData changed,
                                                                                int sourceX, int sourceY, int sourceZ) {
        return new org.bukkit.event.block.BlockPhysicsEvent(block, changed, block.getWorld().getBlockAt(sourceX, sourceY, sourceZ));
    }

    public static org.bukkit.event.block.SignChangeEvent newSignChangeEvent(org.bukkit.block.Block theBlock, Player thePlayer,
                                                                            java.util.List<Component> adventureLines) {
        return new org.bukkit.event.block.SignChangeEvent(theBlock, thePlayer, legacyLines(adventureLines));
    }

    public static org.bukkit.event.block.SignChangeEvent newSignChangeEvent(org.bukkit.block.Block theBlock, Player thePlayer,
                                                                            java.util.List<Component> adventureLines, org.bukkit.block.sign.Side side) {
        return new org.bukkit.event.block.SignChangeEvent(theBlock, thePlayer, legacyLines(adventureLines), side);
    }

    private static String[] legacyLines(java.util.List<Component> lines) {
        String[] legacy = new String[lines.size()];
        for (int i = 0; i < legacy.length; i++) {
            Component line = lines.get(i);
            legacy[i] = line == null ? null : FusionCoreAdventure.toLegacyRgb(line);
        }
        return legacy;
    }

    public static org.bukkit.event.player.PlayerShearEntityEvent newPlayerShearEntityEvent(Player who, org.bukkit.entity.Entity what,
                                                                                           org.bukkit.inventory.ItemStack item,
                                                                                           org.bukkit.inventory.EquipmentSlot hand,
                                                                                           java.util.List<org.bukkit.inventory.ItemStack> drops) {
        return withConstructorArgument(drops, () -> new org.bukkit.event.player.PlayerShearEntityEvent(who, what, item, hand));
    }

    public static org.bukkit.event.block.BlockShearEntityEvent newBlockShearEntityEvent(org.bukkit.block.Block dispenser, org.bukkit.entity.Entity sheared,
                                                                                        org.bukkit.inventory.ItemStack tool,
                                                                                        java.util.List<org.bukkit.inventory.ItemStack> drops) {
        return withConstructorArgument(drops, () -> new org.bukkit.event.block.BlockShearEntityEvent(dispenser, sheared, tool));
    }

    // Paper's WorldCreator constructors with a world key (Paper patch 0500)

    public static org.bukkit.WorldCreator newWorldCreator(String levelName, org.bukkit.NamespacedKey worldKey) {
        if (levelName == null || worldKey == null) {
            throw new IllegalArgumentException("World name and key cannot be null");
        }
        org.bukkit.WorldCreator creator = new org.bukkit.WorldCreator(levelName);
        ((io.izzel.arclight.common.bridge.bukkit.WorldCreatorBridge) creator).fusioncore$setKey(worldKey);
        return creator;
    }

    public static org.bukkit.WorldCreator newWorldCreator(org.bukkit.NamespacedKey worldKey) {
        return newWorldCreator(worldKey.getKey(), worldKey);
    }

    // Paper's cook events with the recipe (patch 0497) and the furnace start with the cook time (patch 0247)

    public static org.bukkit.event.block.BlockCookEvent newBlockCookEvent(org.bukkit.block.Block block, org.bukkit.inventory.ItemStack source,
                                                                          org.bukkit.inventory.ItemStack result, org.bukkit.inventory.CookingRecipe<?> recipe) {
        org.bukkit.event.block.BlockCookEvent event = new org.bukkit.event.block.BlockCookEvent(block, source, result);
        ((io.izzel.arclight.common.bridge.bukkit.BlockCookEventBridge) event).fusioncore$setRecipe(recipe);
        return event;
    }

    public static org.bukkit.event.inventory.FurnaceSmeltEvent newFurnaceSmeltEvent(org.bukkit.block.Block furnace, org.bukkit.inventory.ItemStack source,
                                                                                    org.bukkit.inventory.ItemStack result, org.bukkit.inventory.CookingRecipe<?> recipe) {
        org.bukkit.event.inventory.FurnaceSmeltEvent event = new org.bukkit.event.inventory.FurnaceSmeltEvent(furnace, source, result);
        ((io.izzel.arclight.common.bridge.bukkit.BlockCookEventBridge) event).fusioncore$setRecipe(recipe);
        return event;
    }

    public static org.bukkit.event.inventory.FurnaceStartSmeltEvent newFurnaceStartSmeltEvent(org.bukkit.block.Block furnace, org.bukkit.inventory.ItemStack source,
                                                                                              org.bukkit.inventory.CookingRecipe<?> recipe, int cookingTime) {
        org.bukkit.event.inventory.FurnaceStartSmeltEvent event = new org.bukkit.event.inventory.FurnaceStartSmeltEvent(furnace, source, recipe);
        event.setTotalCookTime(cookingTime);
        return event;
    }

    /** The Bukkit cooking recipe of a vanilla recipe holder, or null (not a cooking recipe, or none). */
    public static org.bukkit.inventory.CookingRecipe<?> cookingRecipe(net.minecraft.world.item.crafting.RecipeHolder<?> holder) {
        return holder != null && ((io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeHolderBridge) (Object) holder).bridge$toBukkitRecipe()
            instanceof org.bukkit.inventory.CookingRecipe<?> recipe ? recipe : null;
    }

    // Paper's MerchantRecipe constructors with the ignore-discounts flag and its copy constructor (patch 0425)

    public static org.bukkit.inventory.MerchantRecipe newMerchantRecipe(org.bukkit.inventory.ItemStack result, int uses, int maxUses, boolean experienceReward,
                                                                        int villagerExperience, float priceMultiplier, boolean ignoreDiscounts) {
        return newMerchantRecipe(result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, 0, 0, ignoreDiscounts);
    }

    public static org.bukkit.inventory.MerchantRecipe newMerchantRecipe(org.bukkit.inventory.ItemStack result, int uses, int maxUses, boolean experienceReward,
                                                                        int villagerExperience, float priceMultiplier, int demand, int specialPrice, boolean ignoreDiscounts) {
        org.bukkit.inventory.MerchantRecipe recipe = new org.bukkit.inventory.MerchantRecipe(result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice);
        ((io.izzel.arclight.common.bridge.bukkit.MerchantRecipeBridge) recipe).setIgnoreDiscounts(ignoreDiscounts);
        return recipe;
    }

    public static org.bukkit.inventory.MerchantRecipe newMerchantRecipe(org.bukkit.inventory.MerchantRecipe recipe) {
        org.bukkit.inventory.MerchantRecipe copy = newMerchantRecipe(recipe.getResult().clone(), recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward(),
            recipe.getVillagerExperience(), recipe.getPriceMultiplier(), recipe.getDemand(), recipe.getSpecialPrice(),
            ((io.izzel.arclight.common.bridge.bukkit.MerchantRecipeBridge) recipe).shouldIgnoreDiscounts());
        copy.setIngredients(recipe.getIngredients());
        return copy;
    }

    // Paper's smithing recipe constructors with the copy-data-components flag (patch 0579)

    @SuppressWarnings("deprecation")
    public static org.bukkit.inventory.SmithingRecipe newSmithingRecipe(org.bukkit.NamespacedKey key, org.bukkit.inventory.ItemStack result,
                                                                        org.bukkit.inventory.RecipeChoice base, org.bukkit.inventory.RecipeChoice addition, boolean copyDataComponents) {
        org.bukkit.inventory.SmithingRecipe recipe = new org.bukkit.inventory.SmithingRecipe(key, result, base, addition);
        ((io.izzel.arclight.common.bridge.core.world.item.crafting.SmithingCopyDataBridge) recipe).fusioncore$setCopyDataComponents(copyDataComponents);
        return recipe;
    }

    public static org.bukkit.inventory.SmithingTransformRecipe newSmithingTransformRecipe(org.bukkit.NamespacedKey key, org.bukkit.inventory.ItemStack result,
                                                                                          org.bukkit.inventory.RecipeChoice template, org.bukkit.inventory.RecipeChoice base,
                                                                                          org.bukkit.inventory.RecipeChoice addition, boolean copyDataComponents) {
        org.bukkit.inventory.SmithingTransformRecipe recipe = new org.bukkit.inventory.SmithingTransformRecipe(key, result, template, base, addition);
        ((io.izzel.arclight.common.bridge.core.world.item.crafting.SmithingCopyDataBridge) recipe).fusioncore$setCopyDataComponents(copyDataComponents);
        return recipe;
    }

    public static org.bukkit.inventory.SmithingTrimRecipe newSmithingTrimRecipe(org.bukkit.NamespacedKey key, org.bukkit.inventory.RecipeChoice template,
                                                                                org.bukkit.inventory.RecipeChoice base, org.bukkit.inventory.RecipeChoice addition, boolean copyDataComponents) {
        org.bukkit.inventory.SmithingTrimRecipe recipe = new org.bukkit.inventory.SmithingTrimRecipe(key, template, base, addition);
        ((io.izzel.arclight.common.bridge.core.world.item.crafting.SmithingCopyDataBridge) recipe).fusioncore$setCopyDataComponents(copyDataComponents);
        return recipe;
    }

    // Fusion Core: Paper's extra constructors of Bukkit value classes

    /** Paper: a vibration without an origin (the origin getter gets a dummy location, as in Paper). */
    public static org.bukkit.Vibration newVibration(org.bukkit.Vibration.Destination destination, int arrivalTime) {
        return new org.bukkit.Vibration(new org.bukkit.Location(null, 0, 0, 0), destination, arrivalTime);
    }

    /** Paper: a modifier for any slot group. */
    public static org.bukkit.attribute.AttributeModifier newAttributeModifier(org.bukkit.NamespacedKey key, double amount, org.bukkit.attribute.AttributeModifier.Operation operation) {
        return new org.bukkit.attribute.AttributeModifier(key, amount, operation, org.bukkit.inventory.EquipmentSlotGroup.ANY);
    }

    /** Paper: a cursor with a component caption, stored as legacy text (RGB kept, click/hover lost). */
    @SuppressWarnings("deprecation")
    public static org.bukkit.map.MapCursor newMapCursor(byte x, byte y, byte direction, byte type, boolean visible, net.kyori.adventure.text.Component caption) {
        return new org.bukkit.map.MapCursor(x, y, direction, type, visible, caption == null ? null : FusionCoreAdventure.toLegacyRgb(caption));
    }

    public static org.bukkit.map.MapCursor newMapCursor(byte x, byte y, byte direction, org.bukkit.map.MapCursor.Type type, boolean visible, net.kyori.adventure.text.Component caption) {
        return new org.bukkit.map.MapCursor(x, y, direction, type, visible, caption == null ? null : FusionCoreAdventure.toLegacyRgb(caption));
    }

    // Fusion Core: Paper's extra constructors of plugin exceptions

    /** Paper (patch 0009 "Paper Plugins"): the message lists the missing plugins. */
    public static org.bukkit.plugin.UnknownDependencyException newUnknownDependencyException(java.util.Collection<String> missingDependencies, String pluginName) {
        return new org.bukkit.plugin.UnknownDependencyException("Unknown/missing dependency plugins: [" + String.join(", ", missingDependencies)
            + "]. Please download and install these plugins to run '" + pluginName + "'.");
    }

    /** Paper's message for a too-long channel name (patch 0480), as a subclass of Spigot's exception. */
    public static org.bukkit.plugin.messaging.ChannelNameTooLongException newChannelNameTooLongException(int length, String shortenedChannel) {
        String message = "Attempted to send a Plugin Message to a channel that was too large. The maximum length a channel may be is "
            + org.bukkit.plugin.messaging.Messenger.MAX_CHANNEL_SIZE + " chars (attempted " + length + " - '" + shortenedChannel + ".";
        return new org.bukkit.plugin.messaging.ChannelNameTooLongException() {
            @Override
            public String getMessage() {
                return message;
            }
        };
    }
}
