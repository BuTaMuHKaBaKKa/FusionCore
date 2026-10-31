package io.izzel.arclight.common.mixin.core.world.entity.monster;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCoreVillagerTargetGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Fusion Core: spigot.yml zombie-aggressive-towards-villager and Purpur's aggressive-towards-villager-when-lagging; the villager
// target goal of zombies (husks, zombie villagers and modded zombies inherit it) and drowned becomes one that checks both
@Mixin({Zombie.class, Drowned.class})
public abstract class ZombieVillagerTargetMixin {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @WrapOperation(method = "addBehaviourGoals", at = @At(value = "NEW", target = "(Lnet/minecraft/world/entity/Mob;Ljava/lang/Class;Z)Lnet/minecraft/world/entity/ai/goal/target/NearestAttackableTargetGoal;"))
    private NearestAttackableTargetGoal fusioncore$villagerGoal(Mob mob, Class targetType, boolean mustSee, Operation<NearestAttackableTargetGoal> original) {
        if (targetType == AbstractVillager.class) {
            return new FusionCoreVillagerTargetGoal<>(mob, targetType, mustSee);
        }
        return original.call(mob, targetType, mustSee);
    }
}
