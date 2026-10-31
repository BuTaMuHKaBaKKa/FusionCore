package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.server.ArclightServer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

/** Fusion Core: Paper's async chunk API. Chunks load on the main thread; the future completes there too. */
@Mixin(value = World.class, remap = false)
public interface WorldMixin extends net.kyori.adventure.audience.ForwardingAudience {

    default CompletableFuture<Chunk> getChunkAtAsync(int x, int z, boolean gen, boolean urgent) {
        World world = (World) this;
        CompletableFuture<Chunk> result = new CompletableFuture<>();
        Runnable load = () -> {
            try {
                result.complete(!gen && !world.isChunkGenerated(x, z) ? null : world.getChunkAt(x, z));
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        };
        if (ArclightServer.isPrimaryThread()) {
            load.run();
        } else {
            ArclightServer.executeOnMainThread(load);
        }
        return result;
    }

    default void getChunkAtAsync(int x, int z, Consumer<? super Chunk> cb) {
        this.getChunkAtAsync(x, z, true).thenAccept(cb).exceptionally(ex -> {
            Bukkit.getLogger().log(Level.WARNING, "Exception in chunk load callback", ex);
            return null;
        });
    }

    default void getChunkAtAsync(int x, int z, boolean gen, Consumer<? super Chunk> cb) {
        this.getChunkAtAsync(x, z, gen).thenAccept(cb).exceptionally(ex -> {
            Bukkit.getLogger().log(Level.WARNING, "Exception in chunk load callback", ex);
            return null;
        });
    }

    default void getChunkAtAsync(Location loc, Consumer<? super Chunk> cb) {
        this.getChunkAtAsync((int) Math.floor(loc.getX()) >> 4, (int) Math.floor(loc.getZ()) >> 4, true, cb);
    }

    default void getChunkAtAsync(Location loc, boolean gen, Consumer<? super Chunk> cb) {
        this.getChunkAtAsync((int) Math.floor(loc.getX()) >> 4, (int) Math.floor(loc.getZ()) >> 4, gen, cb);
    }

    default void getChunkAtAsync(Block block, Consumer<? super Chunk> cb) {
        this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, true, cb);
    }

    default void getChunkAtAsync(Block block, boolean gen, Consumer<? super Chunk> cb) {
        this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, gen, cb);
    }

    default CompletableFuture<Chunk> getChunkAtAsync(Location loc) {
        return this.getChunkAtAsync((int) Math.floor(loc.getX()) >> 4, (int) Math.floor(loc.getZ()) >> 4, true);
    }

    default CompletableFuture<Chunk> getChunkAtAsync(Location loc, boolean gen) {
        return this.getChunkAtAsync((int) Math.floor(loc.getX()) >> 4, (int) Math.floor(loc.getZ()) >> 4, gen);
    }

    default CompletableFuture<Chunk> getChunkAtAsync(Block block) {
        return this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, true);
    }

    default CompletableFuture<Chunk> getChunkAtAsync(Block block, boolean gen) {
        return this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, gen);
    }

    default CompletableFuture<Chunk> getChunkAtAsync(int x, int z) {
        return this.getChunkAtAsync(x, z, true);
    }

    default CompletableFuture<Chunk> getChunkAtAsync(int x, int z, boolean gen) {
        return this.getChunkAtAsync(x, z, gen, false);
    }

    default CompletableFuture<Chunk> getChunkAtAsyncUrgently(Location loc) {
        return this.getChunkAtAsync((int) Math.floor(loc.getX()) >> 4, (int) Math.floor(loc.getZ()) >> 4, true, true);
    }

    default CompletableFuture<Chunk> getChunkAtAsyncUrgently(Location loc, boolean gen) {
        return this.getChunkAtAsync((int) Math.floor(loc.getX()) >> 4, (int) Math.floor(loc.getZ()) >> 4, gen, true);
    }

    default CompletableFuture<Chunk> getChunkAtAsyncUrgently(Block block) {
        return this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, true, true);
    }

    default CompletableFuture<Chunk> getChunkAtAsyncUrgently(Block block, boolean gen) {
        return this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, gen, true);
    }

    default CompletableFuture<Chunk> getChunkAtAsyncUrgently(int x, int z) {
        return this.getChunkAtAsync(x, z, true, true);
    }

    /** Fusion Core: Paper's World is an audience of the players in it (Paper patch 0006 "Adventure"). */
    @Override
    default Iterable<? extends net.kyori.adventure.audience.Audience> audiences() {
        return (java.util.List<? extends net.kyori.adventure.audience.Audience>) (Object) ((org.bukkit.World) this).getPlayers();
    }

    // Fusion Core: copied from Paper's World, where these are default methods over getNearbyEntities
    // Paper start - additional getNearbyEntities API
    /**
     * Gets nearby LivingEntities within the specified radius (bounding box)
     * @param loc Center location
     * @param radius Radius
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double radius) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, loc, radius, radius, radius);
    }

    /**
     * Gets nearby LivingEntities within the specified radius (bounding box)
     * @param loc Center location
     * @param xzRadius X/Z Radius
     * @param yRadius Y Radius
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double xzRadius, final double yRadius) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, loc, xzRadius, yRadius, xzRadius);
    }

    /**
     * Gets nearby LivingEntities within the specified radius (bounding box)
     * @param loc Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z radius
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double xRadius, final double yRadius, final double zRadius) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, loc, xRadius, yRadius, zRadius);
    }

    /**
     * Gets nearby LivingEntities within the specified radius (bounding box)
     * @param loc Center location
     * @param radius X Radius
     * @param predicate a predicate used to filter results
     * @return the collection of living entities near location. This will always be a non-null collection
     */
    default @org.jetbrains.annotations.NotNull java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double radius, final @org.jetbrains.annotations.Nullable java.util.function.Predicate<? super org.bukkit.entity.LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, loc, radius, radius, radius, predicate);
    }

    /**
     * Gets nearby LivingEntities within the specified radius (bounding box)
     * @param loc Center location
     * @param xzRadius X/Z Radius
     * @param yRadius Y Radius
     * @param predicate a predicate used to filter results
     * @return the collection of living entities near location. This will always be a non-null collection
     */
    default @org.jetbrains.annotations.NotNull java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double xzRadius, final double yRadius, final @org.jetbrains.annotations.Nullable java.util.function.Predicate<? super org.bukkit.entity.LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, loc, xzRadius, yRadius, xzRadius, predicate);
    }

    /**
     * Gets nearby LivingEntities within the specified radius (bounding box)
     * @param loc Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z radius
     * @param predicate a predicate used to filter results
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double xRadius, final double yRadius, final double zRadius, final @org.jetbrains.annotations.Nullable java.util.function.Predicate<? super org.bukkit.entity.LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, loc, xRadius, yRadius, zRadius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param radius X/Y/Z Radius
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double radius) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.Player.class, loc, radius, radius, radius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param xzRadius X/Z Radius
     * @param yRadius Y Radius
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double xzRadius, final double yRadius) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.Player.class, loc, xzRadius, yRadius, xzRadius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @return the collection of players near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double xRadius, final double yRadius, final double zRadius) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.Player.class, loc, xRadius, yRadius, zRadius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param radius X/Y/Z Radius
     * @param predicate a predicate used to filter results
     * @return the collection of players near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double radius, final @org.jetbrains.annotations.Nullable java.util.function.Predicate<? super org.bukkit.entity.Player> predicate) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.Player.class, loc, radius, radius, radius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param xzRadius X/Z Radius
     * @param yRadius Y Radius
     * @param predicate a predicate used to filter results
     * @return the collection of players near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double xzRadius, final double yRadius, final @org.jetbrains.annotations.Nullable java.util.function.Predicate<? super org.bukkit.entity.Player> predicate) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.Player.class, loc, xzRadius, yRadius, xzRadius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param loc Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @param predicate a predicate used to filter results
     * @return the collection of players near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double xRadius, final double yRadius, final double zRadius, final @org.jetbrains.annotations.Nullable java.util.function.Predicate<? super org.bukkit.entity.Player> predicate) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.Player.class, loc, xRadius, yRadius, zRadius, predicate);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     * @param clazz Type to filter by
     * @param loc Center location
     * @param radius X/Y/Z radius to search within
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(final @org.jetbrains.annotations.Nullable Class<? extends T> clazz, final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double radius) {
        return this.getNearbyEntitiesByType(clazz, loc, radius, radius, radius, null);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius, with x and x radius matching (bounding box)
     * @param clazz Type to filter by
     * @param loc Center location
     * @param xzRadius X/Z radius to search within
     * @param yRadius Y radius to search within
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(final @org.jetbrains.annotations.Nullable Class<? extends T> clazz, final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double xzRadius, final double yRadius) {
        return this.getNearbyEntitiesByType(clazz, loc, xzRadius, yRadius, xzRadius, null);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     * @param clazz Type to filter by
     * @param loc Center location
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(final @org.jetbrains.annotations.Nullable Class<? extends T> clazz, final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double xRadius, final double yRadius, final double zRadius) {
        return this.getNearbyEntitiesByType(clazz, loc, xRadius, yRadius, zRadius, null);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     * @param clazz Type to filter by
     * @param loc Center location
     * @param radius X/Y/Z radius to search within
     * @param predicate a predicate used to filter results
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(final @org.jetbrains.annotations.Nullable Class<? extends T> clazz, final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double radius, final @org.jetbrains.annotations.Nullable java.util.function.Predicate<? super T> predicate) {
        return this.getNearbyEntitiesByType(clazz, loc, radius, radius, radius, predicate);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius, with x and x radius matching (bounding box)
     * @param clazz Type to filter by
     * @param loc Center location
     * @param xzRadius X/Z radius to search within
     * @param yRadius Y radius to search within
     * @param predicate a predicate used to filter results
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    default @org.jetbrains.annotations.NotNull <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(final @org.jetbrains.annotations.Nullable Class<? extends T> clazz, final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double xzRadius, final double yRadius, final @org.jetbrains.annotations.Nullable java.util.function.Predicate<? super T> predicate) {
        return this.getNearbyEntitiesByType(clazz, loc, xzRadius, yRadius, xzRadius, predicate);
    }

     /**
      * Gets all nearby entities of the specified type, within the specified radius (bounding box)
      * @param clazz Type to filter by
      * @param loc Center location
      * @param xRadius X Radius
      * @param yRadius Y Radius
      * @param zRadius Z Radius
      * @param predicate a predicate used to filter results
      * @param <T> the entity type
      * @return the collection of entities near location. This will always be a non-null collection.
      */
     default <T extends org.bukkit.entity.Entity> @org.jetbrains.annotations.NotNull java.util.Collection<T> getNearbyEntitiesByType(@org.jetbrains.annotations.Nullable Class<? extends org.bukkit.entity.Entity> clazz, final @org.jetbrains.annotations.NotNull org.bukkit.Location loc, final double xRadius, final double yRadius, final double zRadius, final @org.jetbrains.annotations.Nullable java.util.function.Predicate<? super T> predicate) {
         if (clazz == null) {
            clazz = org.bukkit.entity.Entity.class;
        }
        final java.util.List<T> nearby = new java.util.ArrayList<>();
        for (final org.bukkit.entity.Entity bukkitEntity : ((org.bukkit.World) this).getNearbyEntities(loc, xRadius, yRadius, zRadius)) {
            //noinspection unchecked
            if (clazz.isAssignableFrom(bukkitEntity.getClass()) && (predicate == null || predicate.test((T) bukkitEntity))) {
                //noinspection unchecked
                nearby.add((T) bukkitEntity);
            }
        }
        return nearby;
    }
    // Paper end - additional getNearbyEntities API

    // Fusion Core: Paper's particle spawn with explicit receivers, source and force (Paper patch "Expand World.spawnParticle API")
    default <T> void spawnParticle(org.bukkit.Particle particle, java.util.List<org.bukkit.entity.Player> receivers, org.bukkit.entity.Player source,
                                   double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        this.spawnParticle(particle, receivers, source, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, true);
    }

    default <T> void spawnParticle(org.bukkit.Particle particle, java.util.List<org.bukkit.entity.Player> receivers, org.bukkit.entity.Player source,
                                   double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {
        io.izzel.arclight.common.mod.util.FusionCoreWorlds.spawnParticle((org.bukkit.World) this, particle, receivers, source, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, force);
    }

    // Fusion Core: Paper's "Expand Explosions API". Paper can keep the source entity out of the damage
    // (excludeSourceFromDamage); vanilla has no such switch, so here the source takes damage like any entity.
    default boolean createExplosion(org.bukkit.entity.Entity source, org.bukkit.Location loc, float power, boolean setFire, boolean breakBlocks, boolean excludeSourceFromDamage) {
        return ((org.bukkit.World) this).createExplosion(loc, power, setFire, breakBlocks, source);
    }

    default boolean createExplosion(org.bukkit.entity.Entity source, org.bukkit.Location loc, float power, boolean setFire, boolean breakBlocks) {
        return this.createExplosion(source, loc, power, setFire, breakBlocks, true);
    }

    default boolean createExplosion(org.bukkit.entity.Entity source, org.bukkit.Location loc, float power, boolean setFire) {
        return this.createExplosion(source, loc, power, setFire, true);
    }

    default boolean createExplosion(org.bukkit.entity.Entity source, org.bukkit.Location loc, float power) {
        return this.createExplosion(source, loc, power, true, true);
    }

    default boolean createExplosion(org.bukkit.entity.Entity source, float power, boolean setFire, boolean breakBlocks) {
        return this.createExplosion(source, source.getLocation(), power, setFire, breakBlocks);
    }

    default boolean createExplosion(org.bukkit.entity.Entity source, float power, boolean setFire) {
        return this.createExplosion(source, source.getLocation(), power, setFire, true);
    }

    default boolean createExplosion(org.bukkit.entity.Entity source, float power) {
        return this.createExplosion(source, source.getLocation(), power, true, true);
    }

    // Fusion Core: Paper's deprecated callback overloads of getChunkAtAsync, over the future-returning ones (see teleportAsync notes)
    default void getChunkAtAsync(int x, int z, org.bukkit.World$ChunkLoadCallback cb) {
        io.izzel.arclight.common.mod.util.FusionCoreWorlds.getChunkAtAsync((org.bukkit.World) this, x, z, cb);
    }

    default void getChunkAtAsync(org.bukkit.Location loc, org.bukkit.World$ChunkLoadCallback cb) {
        this.getChunkAtAsync(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, cb);
    }

    default void getChunkAtAsync(org.bukkit.block.Block block, org.bukkit.World$ChunkLoadCallback cb) {
        this.getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, cb);
    }

    /** Paper: the world's view distance, applied to the chunk loader like the server's own setting. */
    default void setViewDistance(int viewDistance) {
        if (viewDistance < 2 || viewDistance > 32) {
            throw new IllegalArgumentException("View distance " + viewDistance + " is out of range of [2, 32]");
        }
        ((org.bukkit.craftbukkit.v.CraftWorld) (Object) this).getHandle().getChunkSource().setViewDistance(viewDistance);
    }

    default void setSimulationDistance(int simulationDistance) {
        if (simulationDistance < 2 || simulationDistance > 32) {
            throw new IllegalArgumentException("Simulation distance " + simulationDistance + " is out of range of [2, 32]");
        }
        ((org.bukkit.craftbukkit.v.CraftWorld) (Object) this).getHandle().getChunkSource().setSimulationDistance(simulationDistance);
    }

    /** Paper: name and UUID for Adventure (Paper patch 0010). */
    default net.kyori.adventure.pointer.Pointers pointers() {
        org.bukkit.World self = (org.bukkit.World) this;
        return net.kyori.adventure.pointer.Pointers.builder()
            .withDynamic(net.kyori.adventure.identity.Identity.NAME, self::getName)
            .withDynamic(net.kyori.adventure.identity.Identity.UUID, self::getUID)
            .build();
    }

    // Fusion Core: the rest of Paper's World API (Paper patch numbers at each group)

    // positions and chunks by packed key (API defaults of Paper)

    default boolean isPositionLoaded(io.papermc.paper.math.Position position) {
        return ((org.bukkit.World) this).isChunkLoaded(position.blockX() >> 4, position.blockZ() >> 4);
    }

    default org.bukkit.block.Block getBlockAtKey(long key) {
        return ((org.bukkit.World) this).getBlockAt(io.izzel.arclight.common.mod.util.FusionCoreBlockStatics.getBlockKeyX(key),
            io.izzel.arclight.common.mod.util.FusionCoreBlockStatics.getBlockKeyY(key), io.izzel.arclight.common.mod.util.FusionCoreBlockStatics.getBlockKeyZ(key));
    }

    default org.bukkit.Location getLocationAtKey(long key) {
        return new org.bukkit.Location((org.bukkit.World) this, io.izzel.arclight.common.mod.util.FusionCoreBlockStatics.getBlockKeyX(key),
            io.izzel.arclight.common.mod.util.FusionCoreBlockStatics.getBlockKeyY(key), io.izzel.arclight.common.mod.util.FusionCoreBlockStatics.getBlockKeyZ(key));
    }

    default org.bukkit.Chunk getChunkAt(long chunkKey) {
        return this.getChunkAt(chunkKey, true);
    }

    default org.bukkit.Chunk getChunkAt(long chunkKey, boolean generate) {
        return ((org.bukkit.World) this).getChunkAt((int) chunkKey, (int) (chunkKey >> 32), generate);
    }

    default boolean isChunkGenerated(long chunkKey) {
        return ((org.bukkit.World) this).isChunkGenerated((int) chunkKey, (int) (chunkKey >> 32));
    }

    default org.bukkit.Location locateNearestBiome(org.bukkit.Location origin, org.bukkit.block.Biome biome, int radius) {
        return this.locateNearestBiome(origin, biome, radius, 8);
    }

    default org.bukkit.Location locateNearestBiome(org.bukkit.Location origin, org.bukkit.block.Biome biome, int radius, int step) {
        org.bukkit.util.BiomeSearchResult result = ((org.bukkit.World) this).locateNearestBiome(origin, radius, step, step, biome);
        return result == null ? null : result.getLocation();
    }

    // view distances: vanilla sends the chunks it loads, so the send and no-tick distances are the view distance
    // (as Paper's API defaults before its chunk system rewrite)

    default int getNoTickViewDistance() {
        return ((org.bukkit.World) this).getViewDistance();
    }

    default void setNoTickViewDistance(int viewDistance) {
        this.setViewDistance(viewDistance);
    }

    default int getSendViewDistance() {
        return ((org.bukkit.World) this).getViewDistance();
    }

    default void setSendViewDistance(int viewDistance) {
        this.setViewDistance(viewDistance);
    }

    // patch 0123 "Provide E/TE/Chunk count stat methods"

    default int getEntityCount() {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.entityCount(io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this));
    }

    default int getTileEntityCount() {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.tileEntityCount(io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this));
    }

    default int getTickableTileEntityCount() {
        return ((io.izzel.arclight.common.mixin.core.world.level.LevelPaperAccessor) io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this)).fusioncore$blockEntityTickers().size();
    }

    default int getChunkCount() {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.chunkCount(io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this));
    }

    default int getPlayerCount() {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this).players().size();
    }

    // patch 0516 "More World API", patch 0253 "Add sun related API", patch 0207 "Implement World.getEntity(UUID) API"

    default double getCoordinateScale() {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this).dimensionType().coordinateScale();
    }

    default boolean isFixedTime() {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this).dimensionType().hasFixedTime();
    }

    default java.util.Collection<org.bukkit.Material> getInfiniburn() {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.infiniburn(io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this));
    }

    default void sendGameEvent(org.bukkit.entity.Entity sourceEntity, org.bukkit.GameEvent gameEvent, org.bukkit.util.Vector position) {
        io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this).gameEvent(sourceEntity != null ? ((org.bukkit.craftbukkit.v.entity.CraftEntity) sourceEntity).getHandle() : null,
            net.minecraft.core.registries.BuiltInRegistries.GAME_EVENT.wrapAsHolder(org.bukkit.craftbukkit.v.CraftGameEvent.bukkitToMinecraft(gameEvent)),
            new net.minecraft.world.phys.Vec3(position.getX(), position.getY(), position.getZ()));
    }

    default boolean isDayTime() {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this).isDay();
    }

    default org.bukkit.entity.Entity getEntity(java.util.UUID uuid) {
        com.google.common.base.Preconditions.checkArgument(uuid != null, "UUID cannot be null");
        net.minecraft.world.entity.Entity entity = io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this).getEntity(uuid);
        return entity == null ? null : ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) entity).bridge$getBukkitEntity();
    }

    // patch 0583 "Add methods to find targets for lightning strikes"

    default org.bukkit.Location findLightningRod(org.bukkit.Location location) {
        net.minecraft.server.level.ServerLevel level = io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this);
        return ((io.izzel.arclight.common.bridge.core.server.level.ServerLevelPaperBridge) level)
            .fusioncore$findLightningRod(new net.minecraft.core.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
            .map(pos -> new org.bukkit.Location((org.bukkit.World) this, pos.getX(), pos.getY() - 1, pos.getZ()))
            .orElse(null);
    }

    default org.bukkit.Location findLightningTarget(org.bukkit.Location location) {
        net.minecraft.core.BlockPos pos = io.izzel.arclight.common.mod.util.FusionCoreWorlds.lightningTarget(io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this),
            new net.minecraft.core.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return pos == null ? null : new org.bukkit.Location((org.bukkit.World) this, pos.getX(), pos.getY(), pos.getZ());
    }

    // patch 1064 "Void damage configuration API"

    default boolean isVoidDamageEnabled() {
        return ((io.izzel.arclight.common.bridge.core.server.level.ServerLevelPaperBridge) io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this)).fusioncore$voidDamageEnabled();
    }

    default void setVoidDamageEnabled(boolean enabled) {
        ((io.izzel.arclight.common.bridge.core.server.level.ServerLevelPaperBridge) io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this)).fusioncore$setVoidDamageEnabled(enabled);
    }

    default float getVoidDamageAmount() {
        return ((io.izzel.arclight.common.bridge.core.server.level.ServerLevelPaperBridge) io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this)).fusioncore$voidDamageAmount();
    }

    default void setVoidDamageAmount(float voidDamageAmount) {
        ((io.izzel.arclight.common.bridge.core.server.level.ServerLevelPaperBridge) io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this)).fusioncore$setVoidDamageAmount(voidDamageAmount);
    }

    default double getVoidDamageMinBuildHeightOffset() {
        return ((io.izzel.arclight.common.bridge.core.server.level.ServerLevelPaperBridge) io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this)).fusioncore$voidDamageMinBuildHeightOffset();
    }

    default void setVoidDamageMinBuildHeightOffset(double minBuildHeightOffset) {
        ((io.izzel.arclight.common.bridge.core.server.level.ServerLevelPaperBridge) io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this)).fusioncore$setVoidDamageMinBuildHeightOffset(minBuildHeightOffset);
    }

    // patch 0936 "More Raid API" and patch 0908 "Add Structure check API"

    default org.bukkit.Raid getRaid(int id) {
        net.minecraft.world.entity.raid.Raid raid = io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this).getRaids().raidMap.get(id);
        return raid == null ? null : new org.bukkit.craftbukkit.v.CraftRaid(raid);
    }

    default boolean hasStructureAt(io.papermc.paper.math.Position position, org.bukkit.generator.structure.Structure structure) {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.level(this).structureManager().getStructureWithPieceAt(
            new net.minecraft.core.BlockPos(position.blockX(), position.blockY(), position.blockZ()),
            org.bukkit.craftbukkit.v.generator.structure.CraftStructure.bukkitToMinecraft(structure)).isValid();
    }

    // patch 0898 "Add predicate for blocks when raytracing": Position overloads and a block filter

    default org.bukkit.util.RayTraceResult rayTraceEntities(io.papermc.paper.math.Position start, org.bukkit.util.Vector direction, double maxDistance,
                                                            double raySize, java.util.function.Predicate<? super org.bukkit.entity.Entity> filter) {
        return ((org.bukkit.World) this).rayTraceEntities(io.izzel.arclight.common.mod.util.FusionCoreWorlds.location((org.bukkit.World) this, start),
            direction, maxDistance, raySize, filter);
    }

    default org.bukkit.util.RayTraceResult rayTraceBlocks(io.papermc.paper.math.Position start, org.bukkit.util.Vector direction, double maxDistance,
                                                          org.bukkit.FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks,
                                                          java.util.function.Predicate<? super org.bukkit.block.Block> canCollide) {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.rayTraceBlocks((org.bukkit.World) this, start, direction, maxDistance,
            fluidCollisionMode, ignorePassableBlocks, canCollide);
    }

    default org.bukkit.util.RayTraceResult rayTrace(io.papermc.paper.math.Position start, org.bukkit.util.Vector direction, double maxDistance,
                                                    org.bukkit.FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize,
                                                    java.util.function.Predicate<? super org.bukkit.entity.Entity> filter,
                                                    java.util.function.Predicate<? super org.bukkit.block.Block> canCollide) {
        org.bukkit.util.RayTraceResult blockHit = this.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode, ignorePassableBlocks, canCollide);
        org.bukkit.util.Vector startVec = null;
        double blockHitDistance = maxDistance;
        if (blockHit != null) {
            startVec = new org.bukkit.util.Vector(start.x(), start.y(), start.z());
            // Fusion Core: a mod's block hit reported far away (Sable's constructs) must not widen the entity search
            blockHitDistance = Math.min(maxDistance, startVec.distance(blockHit.getHitPosition()));
        }
        org.bukkit.util.RayTraceResult entityHit = this.rayTraceEntities(start, direction, blockHitDistance, raySize, filter);
        if (blockHit == null) {
            return entityHit;
        }
        if (entityHit == null) {
            return blockHit;
        }
        double entityHitDistanceSquared = startVec.distanceSquared(entityHit.getHitPosition());
        return entityHitDistanceSquared < blockHitDistance * blockHitDistance ? entityHit : blockHit;
    }
}
