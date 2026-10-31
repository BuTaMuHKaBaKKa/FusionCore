package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class OptimizationSpec {

    @Setting("cache-plugin-class")
    private boolean cachePluginClass;

    @Setting("goal-selector-update-interval")
    private int goalSelectorInterval;

    @Setting("use-activation-and-tracking-range")
    private boolean useActivationAndTrackingRange;

    @Setting("explosion-exposure-samples")
    private int explosionExposureSamples = 16;

    @Setting("dab")
    private Dab dab = new Dab();

    @Setting("throttle-mob-spawning")
    private ThrottleMobSpawning throttleMobSpawning = new ThrottleMobSpawning();

    @Setting("only-tick-items-in-hand")
    private boolean onlyTickItemsInHand = false;

    @Setting("reduce-entity-move-packets")
    private boolean reduceEntityMovePackets = false;

    @Setting("async-playerdata-save")
    private boolean asyncPlayerDataSave = false;

    @Setting("villager-lobotomize")
    private VillagerLobotomize villagerLobotomize = new VillagerLobotomize();

    @Setting("optimize-mob-despawn")
    private boolean optimizeMobDespawn = false;

    @Setting("tracker-nearby-players")
    private boolean trackerNearbyPlayers = false;

    @Setting("lagging-threshold")
    private double laggingThreshold = 19.0;

    @Setting("zombie-aggressive-towards-villager-when-lagging")
    private boolean zombieAggressiveTowardsVillagerWhenLagging = true;

    public boolean useActivationAndTrackingRange() {
        return useActivationAndTrackingRange;
    }

    public boolean isCachePluginClass() {
        return cachePluginClass;
    }

    public int getGoalSelectorInterval() {
        return goalSelectorInterval;
    }

    /** Fusion Core: most sample points per axis for an explosion's exposure check of an entity; 0 or less - vanilla */
    public int getExplosionExposureSamples() {
        return explosionExposureSamples;
    }

    public Dab getDab() {
        return dab == null ? new Dab() : dab;
    }

    public ThrottleMobSpawning getThrottleMobSpawning() {
        return throttleMobSpawning == null ? new ThrottleMobSpawning() : throttleMobSpawning;
    }

    public boolean isOnlyTickItemsInHand() {
        return onlyTickItemsInHand;
    }

    public boolean isReduceEntityMovePackets() {
        return reduceEntityMovePackets;
    }

    public boolean isAsyncPlayerDataSave() {
        return asyncPlayerDataSave;
    }

    public VillagerLobotomize getVillagerLobotomize() {
        return villagerLobotomize == null ? new VillagerLobotomize() : villagerLobotomize;
    }

    /** Fusion Core: Leaf's optimize-mob-despawn, the nearest-player search of mob despawning over a per-tick snapshot */
    public boolean isOptimizeMobDespawn() {
        return optimizeMobDespawn;
    }

    /** Fusion Core: the entity tracker checks a moved entity against the players near it only (Paper's nearby players) */
    public boolean isTrackerNearbyPlayers() {
        return trackerNearbyPlayers;
    }

    /** Fusion Core: Purpur's settings.lagging-threshold; the server lags while its one-minute TPS is below it */
    public double getLaggingThreshold() {
        return laggingThreshold;
    }

    /** Fusion Core: Purpur's mobs.zombie.aggressive-towards-villager-when-lagging */
    public boolean isZombieAggressiveTowardsVillagerWhenLagging() {
        return zombieAggressiveTowardsVillagerWhenLagging;
    }

    /** Fusion Core: Pufferfish's Dynamic Activation of Brain as Leaf configures it (performance.dab) */
    @ConfigSerializable
    public static class Dab {

        @Setting("enabled")
        private boolean enabled = false;

        @Setting("start-distance")
        private double startDistance = 12.0;

        @Setting("max-tick-freq")
        private int maxTickFreq = 20;

        @Setting("activation-dist-mod")
        private int activationDistMod = 8;

        @Setting("dont-enable-if-in-water")
        private boolean dontEnableIfInWater = false;

        @Setting("blacklisted-entities")
        private java.util.List<String> blacklistedEntities;

        public boolean isEnabled() {
            return enabled;
        }

        public double getStartDistance() {
            return startDistance;
        }

        public int getMaxTickFreq() {
            return maxTickFreq;
        }

        public int getActivationDistMod() {
            return activationDistMod;
        }

        public boolean isDontEnableIfInWater() {
            return dontEnableIfInWater;
        }

        public java.util.List<String> getBlacklistedEntities() {
            return blacklistedEntities == null ? java.util.List.of() : blacklistedEntities;
        }
    }

    /** Fusion Core: Leaf's performance.throttle-mob-spawning, per spawn category */
    @ConfigSerializable
    public static class ThrottleMobSpawning {

        @Setting("enabled")
        private boolean enabled = false;

        @Setting("categories")
        private java.util.Map<String, Category> categories;

        public boolean isEnabled() {
            return enabled;
        }

        public java.util.Map<String, Category> getCategories() {
            return categories == null ? java.util.Map.of() : categories;
        }

        @ConfigSerializable
        public static class Category {

            @Setting("min-failed")
            private long minFailed = 8;

            @Setting("spawn-chance")
            private double spawnChance = 25.0;

            public long getMinFailed() {
                return minFailed;
            }

            public double getSpawnChance() {
                return spawnChance;
            }
        }
    }

    /** Fusion Core: Purpur's mobs.villager.lobotomize */
    @ConfigSerializable
    public static class VillagerLobotomize {

        @Setting("enabled")
        private boolean enabled = false;

        @Setting("check-interval")
        private int checkInterval = 100;

        @Setting("wait-until-trade-locked")
        private boolean waitUntilTradeLocked = false;

        public boolean isEnabled() {
            return enabled;
        }

        public int getCheckInterval() {
            return checkInterval;
        }

        public boolean isWaitUntilTradeLocked() {
            return waitUntilTradeLocked;
        }
    }
}
