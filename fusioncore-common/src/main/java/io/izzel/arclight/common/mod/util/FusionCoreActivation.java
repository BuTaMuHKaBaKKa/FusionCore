package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.mod.ArclightConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spigotmc.SpigotWorldConfig;

/**
 * Fusion Core: Paper's Entity Activation Range 2.0 (Paper patch 0990, with 0040 and 0305) in place of Spigot's
 * ActivationRange logic. Adds the activation types water, villagers and flying monsters with their own ranges, wakes up
 * inactive mobs a few at a time (wake-up-inactive), gives immunities a length in ticks, lets inactive mobs run their goal
 * selectors every third tick, and drops Spigot's skipping of every fourth tick of active entities. Spigot's own
 * {@code activationType} on the entity is left as it was, since the tracking ranges read it.
 */
public final class FusionCoreActivation {

    public enum Type {
        WATER, VILLAGER, FLYING_MONSTER, RAIDER, MONSTER, ANIMAL, MISC;

        AABB boundingBox = new AABB(0, 0, 0, 0, 0, 0);
    }

    /** Paper's additions to spigot.yml entity-activation-range, read with the rest of the world's Spigot settings */
    public static final class Settings {
        public int flyingMonsterActivationRange = 32;
        public int waterActivationRange = 16;
        public int villagerActivationRange = 32;
        public int wakeUpInactiveAnimals = 4;
        public int wakeUpInactiveAnimalsEvery = 60 * 20;
        public int wakeUpInactiveAnimalsFor = 5 * 20;
        public int wakeUpInactiveMonsters = 8;
        public int wakeUpInactiveMonstersEvery = 20 * 20;
        public int wakeUpInactiveMonstersFor = 5 * 20;
        public int wakeUpInactiveVillagers = 4;
        public int wakeUpInactiveVillagersEvery = 30 * 20;
        public int wakeUpInactiveVillagersFor = 5 * 20;
        public int wakeUpInactiveFlying = 8;
        public int wakeUpInactiveFlyingEvery = 10 * 20;
        public int wakeUpInactiveFlyingFor = 5 * 20;
        public int villagersWorkImmunityAfter = 5 * 20;
        public int villagersWorkImmunityFor = 20;
        public boolean villagersActiveForPanic = true;
    }

    public interface ConfigState {
        Settings fusioncore$activation();
    }

    /** Per-level wake-up budget: animals, flying monsters, monsters, villagers */
    public interface LevelState {
        int[] fusioncore$wakeupRemaining();
    }

    public interface EntityState {
        Type fusioncore$activationType();

        long fusioncore$getActivatedTick();

        void fusioncore$setActivatedTick(long tick);

        long fusioncore$getImmunityTick();

        void fusioncore$setImmunityTick(long tick);

        boolean fusioncore$isTemporarilyActive();

        void fusioncore$setTemporarilyActive(boolean active);

        boolean fusioncore$defaultActivationState();
    }

    /** The block a mob walks to with a MoveToBlockGoal (Paper's PathfinderMob#movingTarget) */
    public interface MovingTarget {
        BlockPos fusioncore$getMovingTarget();

        void fusioncore$setMovingTarget(BlockPos pos);
    }

    private static final Activity[] VILLAGER_PANIC_IMMUNITIES = {Activity.HIDE, Activity.PRE_RAID, Activity.RAID, Activity.PANIC};
    private static AABB maxBB = new AABB(0, 0, 0, 0, 0, 0);

    private FusionCoreActivation() {
    }

    private static Settings settings(Level level) {
        return ((ConfigState) ((WorldBridge) level).bridge$spigotConfig()).fusioncore$activation();
    }

    public static Type typeOf(Entity entity) {
        if (entity instanceof WaterAnimal) {
            return Type.WATER;
        } else if (entity instanceof Villager) {
            return Type.VILLAGER;
        } else if (entity instanceof FlyingMob && entity instanceof Enemy) {
            return Type.FLYING_MONSTER;
        } else if (entity instanceof Raider) {
            return Type.RAIDER;
        } else if (entity instanceof Enemy) {
            return Type.MONSTER;
        } else if (entity instanceof PathfinderMob || entity instanceof AmbientCreature) {
            return Type.ANIMAL;
        }
        return Type.MISC;
    }

    /** Entities that are never deactivated (Spigot's list with Paper's additions, and ranges of 0 or less) */
    public static boolean defaultActive(Entity entity, Type type, SpigotWorldConfig config) {
        Settings s = ((ConfigState) config).fusioncore$activation();
        return type == Type.MISC && config.miscActivationRange <= 0
            || type == Type.RAIDER && config.raiderActivationRange <= 0
            || type == Type.ANIMAL && config.animalActivationRange <= 0
            || type == Type.MONSTER && config.monsterActivationRange <= 0
            || type == Type.VILLAGER && s.villagerActivationRange <= 0
            || type == Type.WATER && s.waterActivationRange <= 0
            || type == Type.FLYING_MONSTER && s.flyingMonsterActivationRange <= 0
            || entity instanceof EyeOfEnder
            || entity instanceof Player
            || entity instanceof ThrowableProjectile
            || entity instanceof EnderDragon
            || entity instanceof EnderDragonPart
            || entity instanceof WitherBoss
            || entity instanceof AbstractHurtingProjectile
            || entity instanceof LightningBolt
            || entity instanceof PrimedTnt
            || entity instanceof FallingBlockEntity
            || entity instanceof AbstractMinecart
            || entity instanceof Boat
            || entity instanceof EndCrystal
            || entity instanceof FireworkRocketEntity
            || entity instanceof ThrownTrident;
    }

    /** Marks the entities around every player as active this tick (Paper's activateEntities, with Pufferfish's DAB priority) */
    public static void activateEntities(Level level) {
        SpigotWorldConfig config = ((WorldBridge) level).bridge$spigotConfig();
        Settings s = settings(level);
        int miscActivationRange = config.miscActivationRange;
        int raiderActivationRange = config.raiderActivationRange;
        int animalActivationRange = config.animalActivationRange;
        int monsterActivationRange = config.monsterActivationRange;
        int waterActivationRange = s.waterActivationRange;
        int flyingActivationRange = s.flyingMonsterActivationRange;
        int villagerActivationRange = s.villagerActivationRange;
        int[] wakeup = ((LevelState) level).fusioncore$wakeupRemaining();
        wakeup[0] = Math.min(wakeup[0] + 1, s.wakeUpInactiveAnimals);
        wakeup[1] = Math.min(wakeup[1] + 1, s.wakeUpInactiveFlying);
        wakeup[2] = Math.min(wakeup[2] + 1, s.wakeUpInactiveMonsters);
        wakeup[3] = Math.min(wakeup[3] + 1, s.wakeUpInactiveVillagers);

        int maxRange = Math.max(monsterActivationRange, animalActivationRange);
        maxRange = Math.max(maxRange, raiderActivationRange);
        maxRange = Math.max(maxRange, miscActivationRange);
        maxRange = Math.max(maxRange, flyingActivationRange);
        maxRange = Math.max(maxRange, waterActivationRange);
        maxRange = Math.max(maxRange, villagerActivationRange);
        maxRange = Math.min((config.simulationDistance << 4) - 8, maxRange);
        boolean dab = FusionCoreTickThrottles.dabEnabled();

        for (Player player : level.players()) {
            ((EntityState) player).fusioncore$setActivatedTick(ArclightConstants.currentTick);
            if (config.ignoreSpectatorActivation && player.isSpectator()) {
                continue;
            }
            // Fusion Core: Purpur's idle-timeout.tick-nearby-entities false, an AFK player activates nothing
            if (FusionCoreAfk.skipsActivation(player)) {
                continue;
            }
            int worldHeight = level.getHeight();
            AABB box = player.getBoundingBox();
            maxBB = box.inflate(maxRange, worldHeight, maxRange);
            Type.MISC.boundingBox = box.inflate(miscActivationRange, worldHeight, miscActivationRange);
            Type.RAIDER.boundingBox = box.inflate(raiderActivationRange, worldHeight, raiderActivationRange);
            Type.ANIMAL.boundingBox = box.inflate(animalActivationRange, worldHeight, animalActivationRange);
            Type.MONSTER.boundingBox = box.inflate(monsterActivationRange, worldHeight, monsterActivationRange);
            Type.WATER.boundingBox = box.inflate(waterActivationRange, worldHeight, waterActivationRange);
            Type.FLYING_MONSTER.boundingBox = box.inflate(flyingActivationRange, worldHeight, flyingActivationRange);
            Type.VILLAGER.boundingBox = box.inflate(villagerActivationRange, worldHeight, villagerActivationRange);
            level.getEntities().get(maxBB, entity -> {
                activateEntity(entity);
                if (dab) {
                    FusionCoreTickThrottles.updatePriority(entity, player);
                }
            });
        }
    }

    private static void activateEntity(Entity entity) {
        EntityState state = (EntityState) entity;
        if (ArclightConstants.currentTick > state.fusioncore$getActivatedTick()) {
            if (state.fusioncore$defaultActivationState()
                || state.fusioncore$activationType().boundingBox.intersects(entity.getBoundingBox())) {
                state.fusioncore$setActivatedTick(ArclightConstants.currentTick);
            }
        }
    }

    private static int checkInactiveWakeup(Entity entity, Settings s) {
        EntityState state = (EntityState) entity;
        int[] wakeup = ((LevelState) entity.level()).fusioncore$wakeupRemaining();
        long inactiveFor = ArclightConstants.currentTick - state.fusioncore$getActivatedTick();
        Type type = state.fusioncore$activationType();
        if (type == Type.VILLAGER) {
            if (inactiveFor > s.wakeUpInactiveVillagersEvery && wakeup[3] > 0) {
                wakeup[3]--;
                return s.wakeUpInactiveVillagersFor;
            }
        } else if (type == Type.ANIMAL) {
            if (inactiveFor > s.wakeUpInactiveAnimalsEvery && wakeup[0] > 0) {
                wakeup[0]--;
                return s.wakeUpInactiveAnimalsFor;
            }
        } else if (type == Type.FLYING_MONSTER) {
            if (inactiveFor > s.wakeUpInactiveFlyingEvery && wakeup[1] > 0) {
                wakeup[1]--;
                return s.wakeUpInactiveFlyingFor;
            }
        } else if (type == Type.MONSTER || type == Type.RAIDER) {
            if (inactiveFor > s.wakeUpInactiveMonstersEvery && wakeup[2] > 0) {
                wakeup[2]--;
                return s.wakeUpInactiveMonstersFor;
            }
        }
        return -1;
    }

    /** Paper's checkEntityImmunities: ticks of immunity the entity gets, or -1 for none */
    public static int immunity(Entity entity) {
        EntityState state = (EntityState) entity;
        Settings s = settings(entity.level());
        int wakeUp = checkInactiveWakeup(entity, s);
        if (wakeUp > -1) {
            return wakeUp;
        }
        if (entity.getRemainingFireTicks() > 0) {
            return 2;
        }
        if (state.fusioncore$getImmunityTick() >= ArclightConstants.currentTick) {
            return 1;
        }
        long inactiveFor = ArclightConstants.currentTick - state.fusioncore$getActivatedTick();
        if (state.fusioncore$activationType() != Type.WATER && entity.wasTouchingWater && entity.isPushedByFluid()) {
            return 100;
        }
        if (!entity.onGround() || entity.getDeltaMovement().horizontalDistanceSqr() > 9.999999747378752E-6D) {
            return 100;
        }
        if (!(entity instanceof AbstractArrow arrow)) {
            if (!entity.onGround() && !(entity instanceof FlyingMob)) {
                return 10;
            }
        } else if (!arrow.inGround) {
            return 1;
        }
        if (entity instanceof LivingEntity living) {
            if (living.onClimbable() || living.jumping || living.hurtTime > 0 || !living.activeEffects.isEmpty() || living.isFreezing()) {
                return 1;
            }
            if (entity instanceof Mob mob && mob.getTarget() != null) {
                return 20;
            }
            if (entity instanceof Bee bee) {
                BlockPos movingTarget = ((MovingTarget) bee).fusioncore$getMovingTarget();
                if (bee.isAngry()
                    || bee.getHivePos() != null && bee.getHivePos().equals(movingTarget)
                    || bee.getSavedFlowerPos() != null && bee.getSavedFlowerPos().equals(movingTarget)) {
                    return 20;
                }
            }
            if (entity instanceof Villager villager) {
                Brain<Villager> brain = villager.getBrain();
                if (s.villagersActiveForPanic) {
                    for (Activity activity : VILLAGER_PANIC_IMMUNITIES) {
                        if (brain.isActive(activity)) {
                            return 20 * 5;
                        }
                    }
                }
                if (s.villagersWorkImmunityAfter > 0 && inactiveFor >= s.villagersWorkImmunityAfter && brain.isActive(Activity.WORK)) {
                    return s.villagersWorkImmunityFor;
                }
            }
            if (entity instanceof Llama llama && llama.inCaravan()) {
                return 1;
            }
            if (entity instanceof Animal animal) {
                if (animal.isBaby() || animal.isInLove()) {
                    return 5;
                }
                if (entity instanceof Sheep sheep && sheep.isSheared()) {
                    return 1;
                }
            }
            if (entity instanceof Creeper creeper && creeper.isIgnited()) {
                return 20;
            }
            if (entity instanceof Mob mob && mob.targetSelector.getAvailableGoals().stream().anyMatch(goal -> goal.isRunning())) {
                return 0;
            }
        }
        // SPIGOT-6644: otherwise the target refresh tick is missed
        if (entity instanceof ExperienceOrb) {
            return 20;
        }
        return -1;
    }

    /** Paper's checkIfActive: whether the entity ticks normally this tick */
    public static boolean checkIfActive(Entity entity) {
        if (entity instanceof FireworkRocketEntity) {
            return true;
        }
        EntityState state = (EntityState) entity;
        // brand new entities, dead entities and portal cases are always active
        if (state.fusioncore$defaultActivationState() || entity.tickCount < 20 * 10 || !entity.isAlive()
            || entity.portalProcess != null && !entity.portalProcess.hasExpired() || entity.portalCooldown > 0) {
            return true;
        }
        if (entity instanceof Mob mob && mob.getLeashHolder() instanceof Player) {
            return true;
        }
        boolean isActive = state.fusioncore$getActivatedTick() >= ArclightConstants.currentTick;
        state.fusioncore$setTemporarilyActive(false);
        if (!isActive) {
            if ((ArclightConstants.currentTick - state.fusioncore$getActivatedTick() - 1) % 20 == 0) {
                // check the immunities every 20 ticks
                int immunity = immunity(entity);
                if (immunity >= 0) {
                    state.fusioncore$setActivatedTick(ArclightConstants.currentTick + immunity);
                } else {
                    state.fusioncore$setTemporarilyActive(true);
                }
                isActive = true;
            } else if (entity instanceof ItemEntity && (entity.tickCount + 1 + entity.getId()) % 4 == 0) {
                // item gravity, see ItemEntity#tick
                isActive = true;
            }
        }
        return isActive;
    }

    /** Keeps an entity active for the next {@code ticks} ticks (pistons, working hopper minecarts) */
    public static void immunize(Entity entity, int ticks) {
        EntityState state = (EntityState) entity;
        state.fusioncore$setActivatedTick(Math.max(state.fusioncore$getActivatedTick(), ArclightConstants.currentTick + ticks));
        state.fusioncore$setImmunityTick(Math.max(state.fusioncore$getImmunityTick(), ArclightConstants.currentTick + ticks));
    }
}
