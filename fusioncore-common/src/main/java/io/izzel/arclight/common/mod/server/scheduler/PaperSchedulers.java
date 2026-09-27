package io.izzel.arclight.common.mod.server.scheduler;

import io.izzel.arclight.common.bridge.bukkit.CraftEntitySchedulerBridge;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.plugin.Plugin;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Fusion Core: the server side of Paper's Folia scheduler API (patch 0843). One tick thread owns every region. */
public final class PaperSchedulers {

    public static final FoliaGlobalRegionScheduler GLOBAL = new FoliaGlobalRegionScheduler();
    public static final FoliaAsyncScheduler ASYNC = new FoliaAsyncScheduler();
    public static final FallbackRegionScheduler REGION = new FallbackRegionScheduler();

    /** Entity schedulers that have tasks; see {@link EntityTaskScheduler}. */
    private static final Set<EntityTaskScheduler> ACTIVE = ConcurrentHashMap.newKeySet();

    private PaperSchedulers() {
    }

    /** Main thread, once per tick right after the Bukkit scheduler, like Paper's {@code MinecraftServer#tickChildren}. */
    public static void tick() {
        GLOBAL.tick();
        if (ACTIVE.isEmpty()) {
            return;
        }
        for (EntityTaskScheduler scheduler : ACTIVE.toArray(EntityTaskScheduler[]::new)) {
            Entity handle = scheduler.entity.getHandle();
            // Paper ticks the schedulers of the entities in each world's entity lookup; "valid" is that set
            if (!handle.isRemoved() && ((EntityBridge) handle).bridge$isValid()) {
                scheduler.executeTick();
            }
        }
    }

    static void setActive(EntityTaskScheduler scheduler, boolean active) {
        if (active) {
            ACTIVE.add(scheduler);
        } else {
            ACTIVE.remove(scheduler);
        }
    }

    /**
     * A new scheduler for a {@code CraftEntity}. Paper creates it with the {@code CraftEntity} and
     * retires it when the entity leaves the server; one created later starts retired if that
     * already happened.
     */
    public static EntityTaskScheduler createScheduler(CraftEntity entity) {
        Entity handle = entity.getHandle();
        Entity.RemovalReason reason = handle.getRemovalReason();
        boolean retired = reason != null && (handle instanceof ServerPlayer
            ? reason == Entity.RemovalReason.UNLOADED_WITH_PLAYER
            : reason != Entity.RemovalReason.CHANGED_DIMENSION);
        return new EntityTaskScheduler(entity, retired);
    }

    /** Paper's {@code Entity#setRemoved} hook: an entity that leaves the server for good (not a player, not a dimension change). */
    public static void onRemoved(Entity entity, Entity.RemovalReason reason, boolean alreadyRemoved) {
        if (!(entity instanceof ServerPlayer) && reason != Entity.RemovalReason.CHANGED_DIMENSION && !alreadyRemoved) {
            retire(entity);
        }
    }

    /** Paper's {@code PlayerList#remove} hook: a player leaves the server. */
    public static void retire(Entity entity) {
        CraftEntity craft = ((EntityBridge) entity).bridge$getBukkitEntityRaw();
        if (craft != null) {
            EntityTaskScheduler scheduler = ((CraftEntitySchedulerBridge) craft).fusioncore$taskSchedulerIfCreated();
            if (scheduler != null) {
                scheduler.retire();
            }
        }
    }

    /** Paper's plugin disable: global and async tasks are cancelled, entity tasks see the plugin disabled and stop. */
    public static void cancelTasks(Plugin plugin) {
        GLOBAL.cancelTasks(plugin);
        ASYNC.cancelTasks(plugin);
    }

    /** Paper's {@code TickThread.isTickThreadFor(...)} for every overload: without regions only the main thread owns anything. */
    public static boolean isOwnedByCurrentRegion() {
        return ArclightServer.isPrimaryThread();
    }

    static VarHandle stateHandle(MethodHandles.Lookup lookup, Class<?> owner) {
        try {
            return lookup.findVarHandle(owner, "state", int.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
