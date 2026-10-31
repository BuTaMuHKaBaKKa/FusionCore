package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.ArclightConstants;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.arclight.i18n.conf.WorldSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Paper's world settings carried out by the core: section {@code world} of fusioncore.conf (the keys of Paper's
 * paper-world-defaults.yml) with {@code world.worlds.<folder>} over it for one world. Each level keeps its resolved
 * {@link Settings}; entity types are resolved once per level, after the registries are frozen. A level that is not a
 * Bukkit world (a mod's own level) or not yet one uses the common values.
 */
public final class FusionCoreWorldSettings {

    private static volatile Settings common;

    private static final ClassValue<String> CONFIG_KEYS = new ClassValue<>() {
        @Override
        protected String computeValue(Class<?> type) {
            String name = type.getName();
            return name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }
    };

    private FusionCoreWorldSettings() {
    }

    /** Implemented by {@link Level} through a mixin: the level's resolved settings */
    public interface Holder {

        Settings fusioncore$worldSettings();
    }

    /** The settings of this level (the common ones for null) */
    public static Settings of(Level level) {
        if (level instanceof Holder holder) {
            return holder.fusioncore$worldSettings();
        }
        return common();
    }

    public static Settings common() {
        Settings s = common;
        if (s == null) {
            s = common = new Settings(ArclightConfig.world(null));
        }
        return s;
    }

    /** Resolves the settings of a Bukkit world by its folder name; null while the level has no Bukkit world yet */
    public static Settings resolve(Level level) {
        org.bukkit.World world = ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) level).bridge$getWorld();
        return world == null ? null : new Settings(ArclightConfig.world(world.getName()));
    }

    public static WorldSpec spec(Level level) {
        return of(level).spec();
    }

    /**
     * Paper's random tick rate check: a rate of 1 always ticks, below 1 never, otherwise only when the server tick
     * plus the position's hash is a multiple of the rate (spreads blocks over the ticks).
     */
    public static boolean skipRandomTick(int rate, BlockPos pos) {
        return rate != 1 && (rate < 1 || (ArclightConstants.currentTick + pos.hashCode()) % rate != 0);
    }

    /** Paper's {@code Level#areChunksLoadedForMove}: every chunk the box touches is loaded, nothing gets loaded */
    public static boolean areChunksLoadedForMove(Level level, AABB box) {
        int minX = ((int) Math.floor(box.minX - 1.0E-7D) - 3) >> 4;
        int maxX = ((int) Math.floor(box.maxX + 1.0E-7D) + 3) >> 4;
        int minZ = ((int) Math.floor(box.minZ - 1.0E-7D) - 3) >> 4;
        int maxZ = ((int) Math.floor(box.maxZ + 1.0E-7D) + 3) >> 4;
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                ChunkAccess chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Paper's hopper.ignore-occluding-blocks: a hopper does not look for container entities (minecarts, chest boats)
     * inside a block that occludes, as Bukkit's {@code Material#isOccluding} decides it.
     */
    public static boolean skipEntityContainers(Level level, net.minecraft.world.level.block.state.BlockState state) {
        return spec(level).getHopper().isIgnoreOccludingBlocks()
            && state.getBlock().defaultBlockState().isRedstoneConductor(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    /** One level's settings and what is resolved from them */
    public static final class Settings {

        private final WorldSpec spec;
        private volatile Map<EntityType<?>, Integer> saveLimits;
        private volatile Map<EntityType<?>, Map<String, Integer>> sensorRates;
        private volatile Map<EntityType<?>, Map<String, Integer>> behaviorRates;

        Settings(WorldSpec spec) {
            this.spec = spec;
        }

        public WorldSpec spec() {
            return spec;
        }

        /** The most entities of this type one chunk saves or loads, -1 without a limit */
        public int saveLimit(EntityType<?> type) {
            Map<EntityType<?>, Integer> map = this.saveLimits;
            if (map == null) {
                map = new HashMap<>();
                for (var entry : spec.getChunks().getEntityPerChunkSaveLimit().entrySet()) {
                    EntityType<?> t = type(entry.getKey());
                    if (t != null && entry.getValue() != null && entry.getValue() > -1) {
                        map.put(t, entry.getValue());
                    }
                }
                this.saveLimits = map;
            }
            return map.getOrDefault(type, -1);
        }

        public boolean hasSaveLimits() {
            saveLimit(EntityType.PIG);
            return !this.saveLimits.isEmpty();
        }

        /** The entities of a chunk with at most {@link #saveLimit} of each type, in their order */
        public ChunkEntities<Entity> limitPerChunk(ChunkEntities<Entity> chunk) {
            if (!hasSaveLimits()) {
                return chunk;
            }
            Map<EntityType<?>, Integer> counts = new HashMap<>();
            List<Entity> kept = new ArrayList<>();
            boolean dropped = false;
            for (Entity entity : (Iterable<Entity>) chunk.getEntities()::iterator) {
                int limit = saveLimit(entity.getType());
                if (limit > -1 && counts.merge(entity.getType(), 1, Integer::sum) > limit) {
                    dropped = true;
                    continue;
                }
                kept.add(entity);
            }
            return dropped ? new ChunkEntities<>(chunk.getPos(), kept) : chunk;
        }

        /** Ticks between scans of a sensor for this entity type, or the sensor's own rate */
        public int sensorRate(EntityType<?> type, Object sensor, int scanRate) {
            Map<EntityType<?>, Map<String, Integer>> map = this.sensorRates;
            if (map == null) {
                map = this.sensorRates = resolveRates(spec.getTickRates().getSensor());
            }
            if (map.isEmpty()) {
                return scanRate;
            }
            Map<String, Integer> rates = map.get(type);
            Integer rate = rates == null ? null : rates.get(CONFIG_KEYS.get(sensor.getClass()));
            return rate == null ? scanRate : rate;
        }

        /** Ticks a behavior of this entity type waits after it last ended, -1 for vanilla */
        public int behaviorRate(EntityType<?> type, Object behavior) {
            Map<EntityType<?>, Map<String, Integer>> map = this.behaviorRates;
            if (map == null) {
                map = this.behaviorRates = resolveRates(spec.getTickRates().getBehavior());
            }
            if (map.isEmpty()) {
                return -1;
            }
            Map<String, Integer> rates = map.get(type);
            Integer rate = rates == null ? null : rates.get(CONFIG_KEYS.get(behavior.getClass()));
            return rate == null ? -1 : rate;
        }
    }

    private static Map<EntityType<?>, Map<String, Integer>> resolveRates(Map<String, Map<String, Integer>> config) {
        Map<EntityType<?>, Map<String, Integer>> map = new HashMap<>();
        for (var entry : config.entrySet()) {
            EntityType<?> t = type(entry.getKey());
            if (t == null || entry.getValue() == null) {
                continue;
            }
            Map<String, Integer> rates = new HashMap<>();
            entry.getValue().forEach((k, v) -> {
                if (v != null) {
                    rates.put(k.toLowerCase(Locale.ROOT), v);
                }
            });
            map.put(t, rates);
        }
        return map;
    }

    private static EntityType<?> type(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id.toLowerCase(Locale.ROOT));
        if (key == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(key)) {
            return null;
        }
        return BuiltInRegistries.ENTITY_TYPE.get(key);
    }
}
