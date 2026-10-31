package io.izzel.arclight.common.mixin.core.world.entity.ai;

import io.izzel.arclight.common.mod.util.FusionCoreTickThrottles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fusion Core: opt-in brain throttles. Pufferfish's DAB (optimization.dab): a brain far from players runs every
// activatedPriority ticks; it throttles every mob with a brain, modded ones too (Pufferfish lists the vanilla ones).
// Purpur's lobotomy (optimization.villager-lobotomize): a villager that cannot walk anywhere keeps its brain off and only
// restocks.
@Mixin(Brain.class)
public abstract class BrainDabMixin {

    @Inject(method = "tick", cancellable = true, at = @At("HEAD"))
    private void fusioncore$throttle(ServerLevel level, LivingEntity entity, CallbackInfo ci) {
        if (entity instanceof Villager villager && FusionCoreTickThrottles.isLobotomized(villager)) {
            if (villager.shouldRestock()) {
                villager.restock();
            }
            ci.cancel();
            return;
        }
        if (!FusionCoreTickThrottles.shouldTickBrain(entity)) {
            ci.cancel();
        }
    }
}
