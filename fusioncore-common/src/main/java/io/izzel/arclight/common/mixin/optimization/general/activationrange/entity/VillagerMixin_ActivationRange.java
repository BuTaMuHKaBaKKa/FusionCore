package io.izzel.arclight.common.mixin.optimization.general.activationrange.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.mixin.optimization.general.activationrange.EntityMixin_ActivationRange;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin_ActivationRange extends EntityMixin_ActivationRange {

    // @formatter:off
    @Shadow protected abstract void customServerAiStep();
    @Shadow protected abstract void maybeDecayGossip();
    // @formatter:on

    // Fusion Core: Paper's Entity Activation Range 2.0, customServerAiStep(inactive = true): no brain, no raid check, no
    // mob AI step; trading and reputation still run
    @Unique private boolean fusioncore$inactiveStep;

    @Override
    public void inactiveTick() {
        Villager villager = (Villager) (Object) this;
        if (villager.getUnhappyCounter() > 0) {
            villager.setUnhappyCounter(villager.getUnhappyCounter() - 1);
        }
        if (villager.isEffectiveAi()) {
            if (((WorldBridge) this.level()).bridge$spigotConfig().tickInactiveVillagers) {
                this.customServerAiStep();
            } else {
                this.fusioncore$inactiveStep = true;
                try {
                    this.customServerAiStep();
                } finally {
                    this.fusioncore$inactiveStep = false;
                }
            }
        }
        this.maybeDecayGossip();
        super.inactiveTick();
    }

    @WrapOperation(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/Brain;tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void fusioncore$inactiveBrain(Brain<?> brain, ServerLevel level, LivingEntity entity, Operation<Void> original) {
        if (!this.fusioncore$inactiveStep) {
            original.call(brain, level, entity);
        }
    }

    @WrapOperation(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private int fusioncore$inactiveRaidCheck(RandomSource random, int bound, Operation<Integer> original) {
        return this.fusioncore$inactiveStep ? 1 : original.call(random, bound);
    }

    @Inject(method = "customServerAiStep", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/AbstractVillager;customServerAiStep()V"))
    private void fusioncore$inactiveSkipMobStep(CallbackInfo ci) {
        if (this.fusioncore$inactiveStep) {
            ci.cancel();
        }
    }
}
