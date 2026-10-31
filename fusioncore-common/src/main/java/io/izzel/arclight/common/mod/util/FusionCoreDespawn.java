package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.ArclightConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fusion Core: Leaf's optimize-mob-despawn, done without replacing {@code checkDespawn}: mobs still run their own despawn
 * code (with NeoForge's despawn event and the overrides of modded mobs); only the search for the nearest player that
 * affects spawning uses a snapshot of the level's players, sorted by x, so a server with many players does not walk every
 * player for every mob. The snapshot is rebuilt every tick and as soon as a player of the level moves, changes game mode,
 * joins or leaves within the tick (a plugin teleporting someone during the mob ticks), so it always matches the live
 * players: the result is the player vanilla's search finds. The snapshot lives on the level (FusionCoreLevelData).
 */
public final class FusionCoreDespawn {

    private FusionCoreDespawn() {
    }

    /** The nearest player that affects spawning, as {@code Level#getNearestPlayer} with no distance limit; server thread only */
    public static Player nearest(Level level, double x, double y, double z) {
        FusionCoreLevelData data = (FusionCoreLevelData) level;
        Index index = (Index) data.fusioncore$despawnIndex();
        if (index == null) {
            data.fusioncore$setDespawnIndex(index = new Index());
        }
        if (index.dirty || index.tick != ArclightConstants.currentTick || index.count != level.players().size()) {
            index.build(level);
        }
        Player best = index.nearest(x, y, z);
        if (best != null && !FusionCorePlayers.affectsSpawning(best)) {
            // eligibility changed without a move (death, affectsSpawning set by a plugin): take a fresh snapshot
            index.build(level);
            best = index.nearest(x, y, z);
        }
        return best;
    }

    /** A player moved, changed game mode or left: the snapshot of its level is out of date */
    public static void invalidate(Level level) {
        if (level instanceof FusionCoreLevelData data && data.fusioncore$despawnIndex() instanceof Index index) {
            index.dirty = true;
        }
    }

    /** Index of the nearest point to (x, y, z) among points sorted by x, -1 for none */
    public static int nearestIndex(double[] xs, double[] ys, double[] zs, double x, double y, double z) {
        int n = xs.length;
        if (n == 0) {
            return -1;
        }
        // start at the first player with x >= the mob's and walk both ways while the x gap alone can still beat the best
        int lo = 0, hi = n;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (xs[mid] < x) lo = mid + 1;
            else hi = mid;
        }
        double best = Double.MAX_VALUE;
        int bestIndex = -1;
        int left = lo - 1, right = lo;
        while (left >= 0 || right < n) {
            double dl = left >= 0 ? x - xs[left] : Double.MAX_VALUE;
            double dr = right < n ? xs[right] - x : Double.MAX_VALUE;
            int i;
            double dx;
            if (dl <= dr) {
                i = left--;
                dx = dl;
            } else {
                i = right++;
                dx = dr;
            }
            if (dx * dx >= best) {
                break;
            }
            double dy = ys[i] - y, dz = zs[i] - z;
            double d = dx * dx + dy * dy + dz * dz;
            if (d < best) {
                best = d;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private static final class Index {

        private int tick = Integer.MIN_VALUE;
        private int count = -1;
        private boolean dirty;
        private Player[] players = new Player[0];
        private double[] xs = new double[0];
        private double[] ys = new double[0];
        private double[] zs = new double[0];

        void build(Level level) {
            this.tick = ArclightConstants.currentTick;
            this.dirty = false;
            this.count = level.players().size();
            List<Player> list = new ArrayList<>();
            for (Player player : level.players()) {
                if (FusionCorePlayers.affectsSpawning(player)) {
                    list.add(player);
                }
            }
            Player[] sorted = list.toArray(new Player[0]);
            Arrays.sort(sorted, (a, b) -> Double.compare(a.getX(), b.getX()));
            int n = sorted.length;
            this.players = sorted;
            this.xs = new double[n];
            this.ys = new double[n];
            this.zs = new double[n];
            for (int i = 0; i < n; i++) {
                xs[i] = sorted[i].getX();
                ys[i] = sorted[i].getY();
                zs[i] = sorted[i].getZ();
            }
        }

        Player nearest(double x, double y, double z) {
            int i = nearestIndex(xs, ys, zs, x, y, z);
            return i < 0 ? null : players[i];
        }
    }
}
