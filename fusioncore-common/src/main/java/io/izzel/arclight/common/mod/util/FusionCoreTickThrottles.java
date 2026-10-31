package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.ArclightConstants;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.arclight.i18n.conf.OptimizationSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Opt-in throttles from Pufferfish, Leaf and Purpur (section {@code optimization} of fusioncore.conf), all off by
 * default: Dynamic Activation of Brain, throttled natural spawning in chunks that keep failing, and lobotomized
 * villagers that cannot walk anywhere.
 */
public final class FusionCoreTickThrottles {

    private static volatile Set<EntityType<?>> dabBlacklist;
    private static volatile long[] minFailed;
    private static volatile long[] spawnChance;

    private FusionCoreTickThrottles() {
    }

    private static OptimizationSpec spec() {
        return ArclightConfig.spec().getOptimization();
    }

    /** Per-entity state: DAB priority and brain counter, villager lobotomy */
    public interface EntityState {

        int fusioncore$getActivatedPriority();

        void fusioncore$setActivatedPriority(int priority);

        int fusioncore$getPriorityTick();

        void fusioncore$setPriorityTick(int tick);

        int fusioncore$nextBehaviorTick();

        boolean fusioncore$isLobotomized();

        void fusioncore$setLobotomized(boolean lobotomized);

        int fusioncore$getNotLobotomizedCount();

        void fusioncore$setNotLobotomizedCount(int count);
    }

    /** Failed natural spawn attempts per spawn category of a chunk (Paper PR "throttle failed spawn attempts") */
    public interface ChunkState {

        long[] fusioncore$failedSpawnAttempts();
    }

    // ---- Dynamic Activation of Brain ----

    public static boolean dabEnabled() {
        return spec().getDab().isEnabled();
    }

    /**
     * Called for every entity in a player's activation box: the first player of the tick resets the priority to the
     * maximum, each player lowers it by distance (Pufferfish's ActivationRange change).
     */
    public static void updatePriority(Entity entity, Player player) {
        // only brains read the priority (BrainDabMixin): items, projectiles, TNT and the like need none
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity)) {
            return;
        }
        EntityState state = (EntityState) entity;
        // already at the lowest this tick: no other player can lower it further
        if (state.fusioncore$getPriorityTick() == ArclightConstants.currentTick && state.fusioncore$getActivatedPriority() == 1) {
            return;
        }
        OptimizationSpec.Dab dab = spec().getDab();
        if (!dab.isEnabled() || isDabBlacklisted(entity.getType())
            || dab.isDontEnableIfInWater() && entity.isInWater() && !entity.getType().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER)) {
            state.fusioncore$setActivatedPriority(1);
            state.fusioncore$setPriorityTick(ArclightConstants.currentTick);
            return;
        }
        int maxPriority = Math.max(1, dab.getMaxTickFreq());
        if (state.fusioncore$getPriorityTick() != ArclightConstants.currentTick) {
            state.fusioncore$setPriorityTick(ArclightConstants.currentTick);
            state.fusioncore$setActivatedPriority(maxPriority);
        }
        double start = dab.getStartDistance();
        int squaredDistance = (int) player.distanceToSqr(entity);
        state.fusioncore$setActivatedPriority(squaredDistance > start * start
            ? Math.max(1, Math.min(squaredDistance >> dab.getActivationDistMod(), state.fusioncore$getActivatedPriority()))
            : 1);
    }

    /** Whether the brain of this entity runs this tick; counts the brain ticks as Pufferfish's behaviorTick does */
    public static boolean shouldTickBrain(Entity entity) {
        if (!dabEnabled()) {
            return true;
        }
        EntityState state = (EntityState) entity;
        int priority = Math.max(1, state.fusioncore$getActivatedPriority());
        return state.fusioncore$nextBehaviorTick() % priority == 0;
    }

    private static boolean isDabBlacklisted(EntityType<?> type) {
        Set<EntityType<?>> set = dabBlacklist;
        if (set == null) {
            set = new HashSet<>();
            for (String name : spec().getDab().getBlacklistedEntities()) {
                ResourceLocation key = ResourceLocation.tryParse(name.toLowerCase(Locale.ROOT));
                if (key != null && BuiltInRegistries.ENTITY_TYPE.containsKey(key)) {
                    set.add(BuiltInRegistries.ENTITY_TYPE.get(key));
                }
            }
            dabBlacklist = set;
        }
        return set.contains(type);
    }

    // ---- throttle-mob-spawning ----

    public static boolean spawnThrottleEnabled() {
        return spec().getThrottleMobSpawning().isEnabled();
    }

    /** Leaf: a chunk that failed at least min-failed times in a row tries this category only with spawn-chance */
    public static boolean skipSpawnAttempt(MobCategory category, ChunkState chunk, net.minecraft.util.RandomSource random) {
        OptimizationSpec.ThrottleMobSpawning throttle = spec().getThrottleMobSpawning();
        if (!throttle.isEnabled()) {
            return false;
        }
        if (minFailed == null) {
            long[] failed = new long[MobCategory.values().length];
            long[] chance = new long[failed.length];
            java.util.Arrays.fill(failed, -1L);
            for (MobCategory c : MobCategory.values()) {
                OptimizationSpec.ThrottleMobSpawning.Category conf = throttle.getCategories().get(c.getSerializedName());
                if (conf != null) {
                    failed[c.ordinal()] = Math.max(-1, conf.getMinFailed());
                    // out of 2^31, the range of the sample below: 100% is never skipped, 0% always
                    chance[c.ordinal()] = Math.round(Math.clamp(conf.getSpawnChance(), 0.0, 100.0) / 100.0 * 2147483648.0);
                }
            }
            spawnChance = chance;
            minFailed = failed;
        }
        long threshold = minFailed[category.ordinal()];
        return threshold >= 0L && chunk.fusioncore$failedSpawnAttempts()[category.ordinal()] >= threshold
            && (random.nextInt() & Integer.MAX_VALUE) >= spawnChance[category.ordinal()];
    }

    public static void recordSpawnAttempt(MobCategory category, ChunkState chunk, int spawned) {
        long[] failed = chunk.fusioncore$failedSpawnAttempts();
        failed[category.ordinal()] = spawned == 0 ? failed[category.ordinal()] + 1 : 0;
    }

    // ---- villager lobotomy (Purpur) ----

    /** Whether the villager's brain stays off this tick: it cannot walk to any side (checked every check-interval) */
    public static boolean isLobotomized(Villager villager) {
        OptimizationSpec.VillagerLobotomize conf = spec().getVillagerLobotomize();
        EntityState state = (EntityState) villager;
        if (!conf.isEnabled()) {
            state.fusioncore$setLobotomized(false);
            return false;
        }
        int interval = Math.max(1, conf.getCheckInterval());
        if (state.fusioncore$getNotLobotomizedCount() > 3) {
            interval *= 2;
        }
        if (villager.level().getGameTime() % interval == 0) {
            boolean lobotomized = !(conf.isWaitUntilTradeLocked() && villager.getVillagerXp() == 0)
                && !canTravelFrom(villager, BlockPos.containing(villager.position().x, villager.getBoundingBox().minY + 0.0625D, villager.position().z));
            state.fusioncore$setLobotomized(lobotomized);
            state.fusioncore$setNotLobotomizedCount(lobotomized ? 0 : state.fusioncore$getNotLobotomizedCount() + 1);
        }
        return state.fusioncore$isLobotomized();
    }

    private static boolean canTravelFrom(Villager villager, BlockPos pos) {
        return canTravelTo(villager, pos.east()) || canTravelTo(villager, pos.west()) || canTravelTo(villager, pos.north()) || canTravelTo(villager, pos.south());
    }

    private static boolean canTravelTo(Villager villager, BlockPos pos) {
        if (villager.level().getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null) {
            return false;
        }
        BlockState bottom = villager.level().getBlockState(pos);
        if (bottom.getBlock() instanceof FenceBlock || bottom.getBlock() instanceof FenceGateBlock || bottom.getBlock() instanceof WallBlock) {
            return false;
        }
        BlockState top = villager.level().getBlockState(pos.above());
        return !((io.izzel.arclight.common.mixin.core.world.level.block.state.BlockBehaviourPaperAccessor) bottom.getBlock()).fusioncore$hasCollision()
            && !((io.izzel.arclight.common.mixin.core.world.level.block.state.BlockBehaviourPaperAccessor) top.getBlock()).fusioncore$hasCollision();
    }
}
