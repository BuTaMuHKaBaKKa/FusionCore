package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.world.server.ChunkMap_TrackedEntityBridge;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fusion Core: the nearby-player lookup of Paper's entity tracker, without threads. When an entity changes section, vanilla
 * runs updatePlayer for every player of the level; a player farther than the entity's effective tracking range (horizontally)
 * never passes that check, so only the players in the chunks within that range, plus those that see the entity now (to be
 * removed), are given to it. The players are put into a grid of chunks once per tracker tick.
 */
public final class FusionCoreTrackerGrid {

    private final Long2ObjectOpenHashMap<List<ServerPlayer>> cells = new Long2ObjectOpenHashMap<>();
    private int playerCount;

    public void build(List<ServerPlayer> players) {
        this.cells.clear();
        this.playerCount = players.size();
        for (ServerPlayer player : players) {
            long key = key(floor(player.getX()) >> 4, floor(player.getZ()) >> 4);
            this.cells.computeIfAbsent(key, k -> new ArrayList<>(2)).add(player);
        }
    }

    public void clear() {
        this.cells.clear();
        this.cells.trim();
        this.playerCount = 0;
    }

    public int playerCount() {
        return playerCount;
    }

    /** The players that may see the tracked entity after its update: those in range, then those that see it now */
    public List<ServerPlayer> candidates(Object tracked, List<ServerPlayer> all) {
        ChunkMap_TrackedEntityBridge bridge = (ChunkMap_TrackedEntityBridge) tracked;
        Entity entity = bridge.bridge$getEntity();
        int range = bridge.fusioncore$effectiveRange();
        int minX = floor(entity.getX() - range - 1) >> 4, maxX = floor(entity.getX() + range + 1) >> 4;
        int minZ = floor(entity.getZ() - range - 1) >> 4, maxZ = floor(entity.getZ() + range + 1) >> 4;
        List<ServerPlayer> result = new ArrayList<>();
        Map<ServerPlayer, Boolean> added = new IdentityHashMap<>();
        if ((long) (maxX - minX + 1) * (maxZ - minZ + 1) > this.cells.size()) {
            // a range wider than the occupied chunks: walk the occupied chunks and keep those inside it (same set)
            for (Long2ObjectMap.Entry<List<ServerPlayer>> cell : this.cells.long2ObjectEntrySet()) {
                long k = cell.getLongKey();
                int cx = (int) k, cz = (int) (k >>> 32);
                if (cx >= minX && cx <= maxX && cz >= minZ && cz <= maxZ) {
                    for (ServerPlayer p : cell.getValue()) {
                        if (added.put(p, Boolean.TRUE) == null) result.add(p);
                    }
                }
            }
        } else {
            for (int cx = minX; cx <= maxX; cx++) {
                for (int cz = minZ; cz <= maxZ; cz++) {
                    List<ServerPlayer> cell = this.cells.get(key(cx, cz));
                    if (cell != null) {
                        for (ServerPlayer p : cell) {
                            if (added.put(p, Boolean.TRUE) == null) result.add(p);
                        }
                    }
                }
            }
        }
        addSeenBy(bridge, result, added);
        return result;
    }

    private static void addSeenBy(ChunkMap_TrackedEntityBridge bridge, List<ServerPlayer> result, Map<ServerPlayer, Boolean> added) {
        for (ServerPlayerConnection connection : bridge.fusioncore$seenBy()) {
            ServerPlayer p = connection.getPlayer();
            if (added.put(p, Boolean.TRUE) == null) result.add(p);
        }
    }

    private static long key(int x, int z) {
        return ((long) x & 0xFFFFFFFFL) | (((long) z & 0xFFFFFFFFL) << 32);
    }

    private static int floor(double v) {
        return (int) Math.floor(v);
    }
}
