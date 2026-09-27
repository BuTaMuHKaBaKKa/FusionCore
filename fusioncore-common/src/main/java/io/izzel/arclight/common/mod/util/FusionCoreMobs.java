package io.izzel.arclight.common.mod.util;

import com.destroystokyo.paper.entity.Pathfinder;
import com.google.common.base.Preconditions;
import io.izzel.arclight.common.mixin.core.world.entity.MobPaperAccessor;
import io.izzel.arclight.common.mixin.core.world.entity.animal.AbstractSchoolingFishAccessor;
import io.izzel.arclight.common.mod.server.entity.ai.PaperPathfinder;
import io.papermc.paper.entity.SchoolableFish;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Fusion Core: bodies of Paper's {@code Mob} API (pathfinder, look control, handedness, experience reward, patches 0244,
 * 0535, 0585, 0811 and 0253), of {@code RangedEntity} (patch 0205) and of {@code SchoolableFish} (patch 0557).
 */
public final class FusionCoreMobs {

    private FusionCoreMobs() {
    }

    public static net.minecraft.world.entity.Mob mob(Object bukkitMob) {
        return (net.minecraft.world.entity.Mob) FusionCoreEntities.handle(bukkitMob);
    }

    /** Paper keeps one pathfinder per {@code CraftMob}; this one is stateless and follows the current handle. */
    public static Pathfinder pathfinder(org.bukkit.entity.Mob bukkitMob) {
        return new PaperPathfinder(bukkitMob);
    }

    public static void lookAt(org.bukkit.entity.Mob bukkitMob, Location location) {
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkArgument(location.getWorld().equals(bukkitMob.getWorld()), "location in a different world");
        mob(bukkitMob).getLookControl().setLookAt(location.getX(), location.getY(), location.getZ());
    }

    public static void lookAt(org.bukkit.entity.Mob bukkitMob, Location location, float headRotationSpeed, float maxHeadPitch) {
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkArgument(location.getWorld().equals(bukkitMob.getWorld()), "location in a different world");
        mob(bukkitMob).getLookControl().setLookAt(location.getX(), location.getY(), location.getZ(), headRotationSpeed, maxHeadPitch);
    }

    public static void lookAt(org.bukkit.entity.Mob bukkitMob, Entity entity) {
        Preconditions.checkNotNull(entity, "entity cannot be null");
        Preconditions.checkArgument(entity.getWorld().equals(bukkitMob.getWorld()), "entity in a different world");
        mob(bukkitMob).getLookControl().setLookAt(((CraftEntity) entity).getHandle());
    }

    public static void lookAt(org.bukkit.entity.Mob bukkitMob, Entity entity, float headRotationSpeed, float maxHeadPitch) {
        Preconditions.checkNotNull(entity, "entity cannot be null");
        Preconditions.checkArgument(entity.getWorld().equals(bukkitMob.getWorld()), "entity in a different world");
        mob(bukkitMob).getLookControl().setLookAt(((CraftEntity) entity).getHandle(), headRotationSpeed, maxHeadPitch);
    }

    /** Paper's {@code CraftMob#isInDaylight}: vanilla's protected {@code Mob#isSunBurnTick}. */
    public static boolean isInDaylight(org.bukkit.entity.Mob bukkitMob) {
        return ((MobPaperAccessor) mob(bukkitMob)).fusioncore$isSunBurnTick();
    }

    /** Paper's {@code CraftMob#getPossibleExperienceReward}: the reward without a killer. */
    public static int possibleExperienceReward(org.bukkit.entity.Mob bukkitMob) {
        net.minecraft.world.entity.Mob handle = mob(bukkitMob);
        return handle.getExperienceReward((ServerLevel) handle.level(), null);
    }

    public static void rangedAttack(Object bukkitMob, LivingEntity target, float charge) {
        ((RangedAttackMob) mob(bukkitMob)).performRangedAttack(FusionCoreEntities.living(target), charge);
    }

    public static AbstractSchoolingFish fish(Object bukkitFish) {
        return (AbstractSchoolingFish) FusionCoreEntities.handle(bukkitFish);
    }

    /** Paper's {@code PaperSchoolableFish#startFollowing}: leaves the old leader first. */
    public static void startFollowing(SchoolableFish follower, SchoolableFish leader) {
        AbstractSchoolingFish handle = fish(follower);
        if (handle.isFollower()) {
            handle.stopFollowing();
        }
        handle.startFollowing(fish(leader));
    }

    public static int schoolSize(SchoolableFish bukkitFish) {
        return ((AbstractSchoolingFishAccessor) fish(bukkitFish)).fusioncore$schoolSize();
    }

    public static SchoolableFish schoolLeader(SchoolableFish bukkitFish) {
        AbstractSchoolingFish leader = ((AbstractSchoolingFishAccessor) fish(bukkitFish)).fusioncore$leader();
        if (leader == null) {
            return null;
        }
        // Fusion Core: a mod fish may have a Bukkit wrapper that is not a SchoolableFish
        return ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) leader).bridge$getBukkitEntity() instanceof SchoolableFish fish ? fish : null;
    }
}
