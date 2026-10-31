package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.Setting;

import java.util.Map;

// Fusion Core: Paper's world settings (config/paper-world-defaults.yml) the core carries out. Keys and defaults are Paper's;
// "worlds" overrides keys for one world by its folder name (ArclightConfig#world), as Paper's world*/paper-world.yml.
@ConfigSerializable
public class WorldSpec {

    @Setting("chunks")
    private Chunks chunks = new Chunks();

    @Setting("collisions")
    private Collisions collisions = new Collisions();

    @Setting("hopper")
    private Hopper hopper = new Hopper();

    @Setting("tick-rates")
    private TickRates tickRates = new TickRates();

    @Setting("feature-seeds")
    private FeatureSeeds featureSeeds = new FeatureSeeds();

    @Setting("unsupported-settings")
    private UnsupportedSettings unsupportedSettings = new UnsupportedSettings();

    @Setting("armorstand")
    private Armorstand armorstand = new Armorstand();

    @Setting("idle-timeout")
    private IdleTimeout idleTimeout = new IdleTimeout();

    @Setting("maps")
    private Maps maps = new Maps();

    public Chunks getChunks() {
        return chunks == null ? new Chunks() : chunks;
    }

    public Collisions getCollisions() {
        return collisions == null ? new Collisions() : collisions;
    }

    public Hopper getHopper() {
        return hopper == null ? new Hopper() : hopper;
    }

    public TickRates getTickRates() {
        return tickRates == null ? new TickRates() : tickRates;
    }

    public FeatureSeeds getFeatureSeeds() {
        return featureSeeds == null ? new FeatureSeeds() : featureSeeds;
    }

    public UnsupportedSettings getUnsupportedSettings() {
        return unsupportedSettings == null ? new UnsupportedSettings() : unsupportedSettings;
    }

    public Armorstand getArmorstand() {
        return armorstand == null ? new Armorstand() : armorstand;
    }

    public IdleTimeout getIdleTimeout() {
        return idleTimeout == null ? new IdleTimeout() : idleTimeout;
    }

    public Maps getMaps() {
        return maps == null ? new Maps() : maps;
    }

    /** Paper's maps section of paper-world-defaults.yml */
    @ConfigSerializable
    public static class Maps {

        @Setting("item-frame-cursor-update-interval")
        private int itemFrameCursorUpdateInterval = 10;

        /** Ticks between map updates sent for a map in an item frame; 0 or less - none */
        public int getItemFrameCursorUpdateInterval() {
            return itemFrameCursorUpdateInterval;
        }
    }

    /** Purpur's gameplay-mechanics.player.idle-timeout (AFK after server.properties player-idle-timeout minutes) */
    @ConfigSerializable
    public static class IdleTimeout {

        @Setting("kick-if-idle")
        private boolean kickIfIdle = true;

        @Setting("tick-nearby-entities")
        private boolean tickNearbyEntities = true;

        @Setting("mobs-target")
        private boolean mobsTarget = true;

        public boolean isKickIfIdle() {
            return kickIfIdle;
        }

        public boolean isTickNearbyEntities() {
            return tickNearbyEntities;
        }

        public boolean isMobsTarget() {
            return mobsTarget;
        }
    }

    /** Paper's feature-seeds: own random seeds for the configured features of each world, kept in a file in the world folder */
    @ConfigSerializable
    public static class FeatureSeeds {

        @Setting("generate-random-seeds-for-all")
        private boolean generateRandomSeedsForAll = false;

        public boolean isGenerateRandomSeedsForAll() {
            return generateRandomSeedsForAll;
        }
    }

    /** Paper's unsupported-settings (paper-global.yml) */
    @ConfigSerializable
    public static class UnsupportedSettings {

        @Setting("allow-headless-pistons")
        private boolean allowHeadlessPistons = false;

        public boolean isAllowHeadlessPistons() {
            return allowHeadlessPistons;
        }
    }

    /** Purpur's gameplay-mechanics.armorstand */
    @ConfigSerializable
    public static class Armorstand {

        @Setting("place-with-arms-visible")
        private boolean placeWithArmsVisible = false;

        @Setting("tick")
        private boolean tick = true;

        @Setting("minecart-collision-lookups")
        private boolean minecartCollisionLookups = true;

        /** Paper's entities.armor-stands.tick: false - stands without their own setting do not tick (no gravity, no fire) */
        public boolean isTick() {
            return tick;
        }

        /** The minecart part of Paper's armor-stands.do-collision-entity-lookups: false - stands do not look for minecarts to push */
        public boolean isMinecartCollisionLookups() {
            return minecartCollisionLookups;
        }

        public boolean isPlaceWithArmsVisible() {
            return placeWithArmsVisible;
        }
    }

    @ConfigSerializable
    public static class Chunks {

        @Setting("entity-per-chunk-save-limit")
        private Map<String, Integer> entityPerChunkSaveLimit;

        @Setting("prevent-moving-into-unloaded-chunks")
        private boolean preventMovingIntoUnloadedChunks = false;

        @Setting("max-auto-save-chunks-per-tick")
        private int maxAutoSaveChunksPerTick = 24;

        /** At most this many chunks are saved per tick after an autosave; 0 or less saves them all at once (vanilla) */
        public int getMaxAutoSaveChunksPerTick() {
            return maxAutoSaveChunksPerTick;
        }

        /** Entity type id (namespace optional) to the most entities of that type a chunk saves and loads; -1 - no limit */
        public Map<String, Integer> getEntityPerChunkSaveLimit() {
            return entityPerChunkSaveLimit == null ? Map.of() : entityPerChunkSaveLimit;
        }

        public boolean isPreventMovingIntoUnloadedChunks() {
            return preventMovingIntoUnloadedChunks;
        }
    }

    @ConfigSerializable
    public static class Collisions {

        @Setting("max-entity-collisions")
        private int maxEntityCollisions = 8;

        public int getMaxEntityCollisions() {
            return maxEntityCollisions;
        }
    }

    @ConfigSerializable
    public static class Hopper {

        @Setting("ignore-occluding-blocks")
        private boolean ignoreOccludingBlocks = false;

        public boolean isIgnoreOccludingBlocks() {
            return ignoreOccludingBlocks;
        }
    }

    @ConfigSerializable
    public static class TickRates {

        @Setting("grass-spread")
        private int grassSpread = 1;

        @Setting("container-update")
        private int containerUpdate = 1;

        @Setting("mob-spawner")
        private int mobSpawner = 1;

        @Setting("wet-farmland")
        private int wetFarmland = 1;

        @Setting("dry-farmland")
        private int dryFarmland = 1;

        @Setting("sensor")
        private Map<String, Map<String, Integer>> sensor;

        @Setting("behavior")
        private Map<String, Map<String, Integer>> behavior;

        public int getGrassSpread() {
            return grassSpread;
        }

        public int getContainerUpdate() {
            return containerUpdate;
        }

        public int getMobSpawner() {
            return mobSpawner;
        }

        public int getWetFarmland() {
            return wetFarmland;
        }

        public int getDryFarmland() {
            return dryFarmland;
        }

        /** Entity type id to sensor class name (lower case, no package) to ticks between scans */
        public Map<String, Map<String, Integer>> getSensor() {
            return sensor == null ? Map.of() : sensor;
        }

        /** Entity type id to behavior class name (lower case, no package) to ticks after the last run; -1 - vanilla */
        public Map<String, Map<String, Integer>> getBehavior() {
            return behavior == null ? Map.of() : behavior;
        }
    }
}
