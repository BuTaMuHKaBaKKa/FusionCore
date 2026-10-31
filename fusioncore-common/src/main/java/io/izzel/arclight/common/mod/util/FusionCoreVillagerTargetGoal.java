package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.server.MinecraftServerBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.i18n.ArclightConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

/**
 * Fusion Core: the villager target goal of zombies and drowned. spigot.yml {@code zombie-aggressive-towards-villager} (Spigot,
 * Paper patch 0257 for drowned) turns it off; Purpur's {@code aggressive-towards-villager-when-lagging} turns it off while the
 * server's one-minute TPS is below {@code lagging-threshold}. Both are read live, so a reload applies to zombies already alive.
 */
public class FusionCoreVillagerTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

    public FusionCoreVillagerTargetGoal(Mob mob, Class<T> targetType, boolean mustSee) {
        super(mob, targetType, mustSee);
    }

    private boolean allowed() {
        if (!((WorldBridge) this.mob.level()).bridge$spigotConfig().zombieAggressiveTowardsVillager) {
            return false;
        }
        var spec = ArclightConfig.spec().getOptimization();
        return spec.isZombieAggressiveTowardsVillagerWhenLagging() || !isLagging(this.mob.level().getServer(), spec.getLaggingThreshold());
    }

    public static boolean isLagging(MinecraftServer server, double threshold) {
        return server != null && ((MinecraftServerBridge) server).bridge$getRecentTps()[0] < threshold;
    }

    @Override
    public boolean canUse() {
        return allowed() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return allowed() && super.canContinueToUse();
    }
}
