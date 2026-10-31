package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.i18n.ArclightConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Fusion Core: {@code Explosion#getSeenPercent} casts (2w+1)(2h+1)(2d+1) rays through the entity's box. For an entity the size of a
 * ship (Create contraptions, physics constructs) that is hundreds of thousands of rays per explosion, and a mod's damage calculator
 * may ask a second time (Create Big Cannons does). With {@code optimization.explosion-exposure-samples} above zero:
 * <ul>
 * <li>at most that many sample points per axis; vanilla entities stay under the default cap, so their result is unchanged;</li>
 * <li>while one explosion runs, the answer for an entity is kept, so a mod's damage calculator asking again (Create Big Cannons
 * does, vanilla asks once per entity) gets the same number without casting the rays again. The cache is emptied when the
 * explosion ends: nothing is reused across explosions or kept afterwards. Server thread only.</li>
 * </ul>
 */
public final class FusionCoreExplosions {

    private record Key(Entity entity, double x, double y, double z, AABB box) {
    }

    private static final Map<Key, Float> CACHE = new HashMap<>();
    private static int depth;

    private FusionCoreExplosions() {
    }

    /** An explosion starts computing (Explosion#explode); server thread */
    public static void begin() {
        if (depth++ == 0) {
            CACHE.clear();
        }
    }

    /** The explosion is done; its answers are dropped */
    public static void end() {
        if (depth > 0 && --depth == 0) {
            CACHE.clear();
        }
    }

    public static float seenPercent(Vec3 center, Entity entity, Supplier<Float> vanilla) {
        int cap = ArclightConfig.spec().getOptimization().getExplosionExposureSamples();
        MinecraftServer server = ArclightServer.getMinecraftServer();
        if (cap <= 0 || server == null || !server.isSameThread() || entity.level().isClientSide) {
            return vanilla.get();
        }
        if (depth == 0) {
            return capped(center, entity, cap, vanilla);
        }
        Key key = new Key(entity, center.x, center.y, center.z, entity.getBoundingBox());
        Float cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        float result = capped(center, entity, cap, vanilla);
        CACHE.put(key, result);
        return result;
    }

    private static float capped(Vec3 center, Entity entity, int cap, Supplier<Float> vanilla) {
        AABB box = entity.getBoundingBox();
        double dx = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double dy = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double dz = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double minStep = 1.0 / Math.max(1, cap - 1);
        if (dx < 0.0 || dy < 0.0 || dz < 0.0 || dx >= minStep && dy >= minStep && dz >= minStep) {
            return vanilla.get();
        }
        // the vanilla loop with a coarser step on the axes that are too long
        dx = Math.max(dx, minStep);
        dy = Math.max(dy, minStep);
        dz = Math.max(dz, minStep);
        double offX = (1.0 - Math.floor(1.0 / dx) * dx) / 2.0;
        double offZ = (1.0 - Math.floor(1.0 / dz) * dz) / 2.0;
        Level level = entity.level();
        int seen = 0;
        int total = 0;
        for (double fx = 0.0; fx <= 1.0; fx += dx) {
            for (double fy = 0.0; fy <= 1.0; fy += dy) {
                for (double fz = 0.0; fz <= 1.0; fz += dz) {
                    Vec3 from = new Vec3(Mth.lerp(fx, box.minX, box.maxX) + offX, Mth.lerp(fy, box.minY, box.maxY), Mth.lerp(fz, box.minZ, box.maxZ) + offZ);
                    if (level.clip(new ClipContext(from, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS) {
                        seen++;
                    }
                    total++;
                }
            }
        }
        return total == 0 ? 0.0F : (float) seen / (float) total;
    }
}
