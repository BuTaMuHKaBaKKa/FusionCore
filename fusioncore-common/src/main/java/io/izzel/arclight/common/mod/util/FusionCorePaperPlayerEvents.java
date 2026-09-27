package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.server.network.ServerLoginPacketListenerImplBridge;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ServerItemCooldowns;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.util.CraftLocation;
import org.bukkit.craftbukkit.v.util.CraftMagicNumbers;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/** Fusion Core: Paper's player events fired from the core. Events nobody listens to are not built. */
public final class FusionCorePaperPlayerEvents {

    private FusionCorePaperPlayerEvents() {
    }

    private static boolean listened(HandlerList handlers) {
        return handlers.getRegisteredListeners().length > 0;
    }

    private static org.bukkit.entity.Entity bukkit(Entity entity) {
        return ((EntityBridge) entity).bridge$getBukkitEntity();
    }

    private static org.bukkit.entity.Player bukkit(ServerPlayer player) {
        return (org.bukkit.entity.Player) bukkit((Entity) player);
    }

    private static boolean call(org.bukkit.event.Event event) {
        Bukkit.getPluginManager().callEvent(event);
        return !(event instanceof org.bukkit.event.Cancellable cancellable) || !cancellable.isCancelled();
    }

    public static void updateInventory(@Nullable LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            bukkit(player).updateInventory();
        }
    }

    // ---- PlayerLaunchProjectileEvent ----

    private static boolean keepLaunched;

    /**
     * PlayerLaunchProjectileEvent for {@code projectile} thrown by {@code user} with {@code stack}: false when the launch
     * is cancelled. Whether the item is used up is remembered for {@link #consumeLaunched}.
     */
    public static boolean launchProjectile(@Nullable LivingEntity user, ItemStack stack, Entity projectile) {
        keepLaunched = false;
        if (!(user instanceof ServerPlayer player) || !listened(com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent.getHandlerList())
            || !(bukkit(projectile) instanceof org.bukkit.entity.Projectile bukkitProjectile)) {
            return true;
        }
        var event = new com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent(bukkit(player), CraftItemStack.asCraftMirror(stack), bukkitProjectile);
        if (!call(event)) {
            return false;
        }
        keepLaunched = !event.shouldConsume();
        return true;
    }

    /** Whether the last launched item is to be used up (does not reset). */
    public static boolean shouldConsumeLaunched() {
        return !keepLaunched;
    }

    /** Whether the last launched item is to be used up; when not, the thrower's inventory is resent. Resets. */
    public static boolean consumeLaunched(@Nullable LivingEntity user) {
        if (!keepLaunched) {
            return true;
        }
        keepLaunched = false;
        updateInventory(user);
        return false;
    }

    // ---- PlayerBedFailEnterEvent ----

    /** The event for a failed attempt to sleep; null when it is cancelled (nothing happens then). */
    public static io.papermc.paper.event.player.PlayerBedFailEnterEvent bedFailEnter(Player player, Level level, BlockPos pos, Player.BedSleepingProblem problem) {
        var reasons = io.papermc.paper.event.player.PlayerBedFailEnterEvent.FailReason.values();
        int ordinal = problem.ordinal();
        var event = new io.papermc.paper.event.player.PlayerBedFailEnterEvent((org.bukkit.entity.Player) bukkit(player),
            ordinal < reasons.length ? reasons[ordinal] : io.papermc.paper.event.player.PlayerBedFailEnterEvent.FailReason.OTHER_PROBLEM,
            CraftBlock.at(level, pos), !level.dimensionType().bedWorks(),
            problem.getMessage() == null ? null : FusionCoreAdventure.asAdventure(problem.getMessage()));
        if (!(player instanceof ServerPlayer) || !listened(io.papermc.paper.event.player.PlayerBedFailEnterEvent.getHandlerList())) {
            return event;
        }
        return call(event) ? event : null;
    }

    // ---- PlayerInsertLecternBookEvent ----

    private static ItemStack insertedBook;

    /** False when the book is not to be put on the lectern; the book to put there is kept for {@link #takeInsertedBook}. */
    public static boolean insertLecternBook(@Nullable LivingEntity user, Level level, BlockPos pos, ItemStack stack) {
        insertedBook = null;
        if (!(user instanceof ServerPlayer player) || !listened(io.papermc.paper.event.player.PlayerInsertLecternBookEvent.getHandlerList())) {
            return true;
        }
        var event = new io.papermc.paper.event.player.PlayerInsertLecternBookEvent(bukkit(player), CraftBlock.at(level, pos),
            CraftItemStack.asCraftMirror(stack.copyWithCount(1)));
        if (!call(event)) {
            return false;
        }
        insertedBook = CraftItemStack.asNMSCopy(event.getBook());
        return true;
    }

    public static @Nullable ItemStack takeInsertedBook() {
        ItemStack book = insertedBook;
        insertedBook = null;
        return book;
    }

    // ---- PlayerNameEntityEvent ----

    /** Names the entity through the event; null when nobody listens (vanilla goes on). */
    public static @Nullable InteractionResult nameEntity(ItemStack stack, Player player, LivingEntity target) {
        if (!(player instanceof ServerPlayer serverPlayer) || !listened(io.papermc.paper.event.player.PlayerNameEntityEvent.getHandlerList())) {
            return null;
        }
        var event = new io.papermc.paper.event.player.PlayerNameEntityEvent(bukkit(serverPlayer), (org.bukkit.entity.LivingEntity) bukkit(target),
            FusionCoreAdventure.asAdventure(stack.getHoverName()), true);
        if (!call(event)) {
            return InteractionResult.PASS;
        }
        LivingEntity named = ((org.bukkit.craftbukkit.v.entity.CraftLivingEntity) event.getEntity()).getHandle();
        named.setCustomName(event.getName() != null ? FusionCoreAdventure.asVanilla(event.getName()) : null);
        if (event.isPersistent() && named instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        stack.shrink(1);
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    // ---- PlayerItemFrameChangeEvent ----

    /**
     * The item the frame gets for {@code action} (REMOVE, PLACE, ROTATE): {@code item} itself when nobody listens, the
     * event's item otherwise, null when the change is cancelled.
     */
    public static @Nullable ItemStack itemFrameChange(Player player, ItemFrame frame, ItemStack item, String action) {
        if (!(player instanceof ServerPlayer serverPlayer) || !listened(io.papermc.paper.event.player.PlayerItemFrameChangeEvent.getHandlerList())) {
            return item;
        }
        var event = new io.papermc.paper.event.player.PlayerItemFrameChangeEvent(bukkit(serverPlayer), (org.bukkit.entity.ItemFrame) bukkit(frame),
            CraftItemStack.asBukkitCopy(item), io.papermc.paper.event.player.PlayerItemFrameChangeEvent.ItemFrameChangeAction.valueOf(action));
        return call(event) ? CraftItemStack.asNMSCopy(event.getItemStack()) : null;
    }

    // ---- PlayerTeleportEndGatewayEvent ----

    /** The (already called) event when the player is going through an end gateway, null for any other same-world move. */
    public static com.destroystokyo.paper.event.player.@Nullable PlayerTeleportEndGatewayEvent endGatewayTeleport(ServerPlayer player, DimensionTransition transition) {
        net.minecraft.world.entity.PortalProcessor process = player.portalProcess;
        if (process == null || !process.isSamePortal((net.minecraft.world.level.block.Portal) net.minecraft.world.level.block.Blocks.END_GATEWAY)
            || !(player.serverLevel().getBlockEntity(process.getEntryPosition()) instanceof net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity gateway)) {
            return null;
        }
        org.bukkit.entity.Player bukkit = bukkit(player);
        org.bukkit.Location to = CraftLocation.toBukkit(transition.pos(), ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) player.serverLevel()).bridge$getWorld(), transition.yRot(), transition.xRot());
        var event = new com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent(bukkit, bukkit.getLocation(), to,
            new org.bukkit.craftbukkit.v.block.CraftEndGateway(to.getWorld(), gateway));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    // ---- PlayerPostRespawnEvent ----

    public static void postRespawn(ServerPlayer player, DimensionTransition transition, boolean keepEverything) {
        if (!listened(com.destroystokyo.paper.event.player.PlayerPostRespawnEvent.getHandlerList())) {
            return;
        }
        boolean bedSpawn = false;
        if (!keepEverything) {
            BlockPos pos = BlockPos.containing(transition.pos());
            bedSpawn = transition.newLevel().getBlockState(pos).is(net.minecraft.tags.BlockTags.BEDS) && !transition.missingRespawnBlock();
        }
        org.bukkit.Location location = CraftLocation.toBukkit(transition.pos(), ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) transition.newLevel()).bridge$getWorld(), transition.yRot(), transition.xRot());
        Bukkit.getPluginManager().callEvent(new com.destroystokyo.paper.event.player.PlayerPostRespawnEvent(bukkit(player), location, bedSpawn));
    }

    // ---- PlayerAttackEntityCooldownResetEvent ----

    /** Resets the attacker's attack cooldown after a hit unless the event says to keep it. */
    public static void resetAttackStrength(Player attacker, LivingEntity victim) {
        if (attacker instanceof ServerPlayer player && listened(com.destroystokyo.paper.event.player.PlayerAttackEntityCooldownResetEvent.getHandlerList())) {
            if (!call(new com.destroystokyo.paper.event.player.PlayerAttackEntityCooldownResetEvent(bukkit(player), bukkit(victim), player.getAttackStrengthScale(0F)))) {
                return;
            }
        }
        attacker.resetAttackStrengthTicker();
    }

    // ---- PlayerItemCooldownEvent / PlayerShieldDisableEvent ----

    /** The cooldown to set, -1 when the event cancels it. Only a player's server-side cooldowns fire the event. */
    public static int itemCooldown(ItemCooldowns cooldowns, Item item, int ticks) {
        if (!(cooldowns instanceof ServerItemCooldowns server) || !listened(io.papermc.paper.event.player.PlayerItemCooldownEvent.getHandlerList())) {
            return ticks;
        }
        var event = new io.papermc.paper.event.player.PlayerItemCooldownEvent(bukkit(server.player), CraftMagicNumbers.getMaterial(item), ticks);
        return call(event) ? event.getCooldown() : -1;
    }

    /** Set around {@code Player#disableShield()} when the shield cooldown comes from PlayerShieldDisableEvent; -1 otherwise. */
    public static int pendingShieldCooldown = -1;

    /** The shield cooldown for an attacker disabling the player's shield, -1 when the event cancels the disabling. */
    public static int shieldDisable(Player player, LivingEntity attacker) {
        if (!(player instanceof ServerPlayer serverPlayer) || !listened(io.papermc.paper.event.player.PlayerShieldDisableEvent.getHandlerList())) {
            return 100;
        }
        var event = new io.papermc.paper.event.player.PlayerShieldDisableEvent(bukkit(serverPlayer), bukkit(attacker), 100);
        return call(event) ? event.getCooldown() : -1;
    }

    // ---- PlayerConnectionCloseEvent ----

    /** After a connection was closed: players in the game or configuration phase, and logins past authentication. */
    public static void connectionClosed(Connection connection, @Nullable PacketListener listener) {
        if (!listened(com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent.getHandlerList())
            || !(connection.getRemoteAddress() instanceof InetSocketAddress address)) {
            return;
        }
        com.mojang.authlib.GameProfile profile = null;
        if (listener instanceof ServerCommonPacketListenerImpl common) {
            profile = common.getOwner();
        } else if (listener instanceof ServerLoginPacketListenerImplBridge login) {
            profile = login.bridge$authenticatedProfileAfterAuth();
        }
        if (profile != null) {
            Bukkit.getPluginManager().callEvent(new com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent(profile.getId(), profile.getName(),
                address.getAddress(), false));
        }
    }

    // ---- AsyncChatDecorateEvent / AsyncChatCommandDecorateEvent ----

    /**
     * The decorated message: {@code message} as it is when nobody listens, otherwise the result of the event, fired on the
     * chat executor (the event is asynchronous); a cancelled event keeps {@code message}.
     */
    public static CompletableFuture<Component> decorateChat(@Nullable ServerPlayer sender, boolean command, Component message) {
        HandlerList handlers = command ? io.papermc.paper.event.player.AsyncChatCommandDecorateEvent.getHandlerList()
            : io.papermc.paper.event.player.AsyncChatDecorateEvent.getHandlerList();
        if (!listened(handlers)) {
            return CompletableFuture.completedFuture(message);
        }
        return CompletableFuture.supplyAsync(() -> {
            org.bukkit.entity.Player player = sender == null ? null : bukkit(sender);
            net.kyori.adventure.text.Component initial = FusionCoreAdventure.asAdventure(message);
            var event = command ? new io.papermc.paper.event.player.AsyncChatCommandDecorateEvent(player, initial)
                : new io.papermc.paper.event.player.AsyncChatDecorateEvent(player, initial);
            return call(event) ? FusionCoreAdventure.asVanilla(event.result()) : message;
        }, ArclightServer.getChatExecutor());
    }
}
