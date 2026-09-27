package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCoreExperienceOrbs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.bukkit.entity.ExperienceOrb$SpawnReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fusion Core: Paper's orb reason for block experience (patch 0125): {@code BLOCK_BREAK}; callers that know who broke the
 * block (player breaking, NeoForge's block drops) set the trigger around the call.
 */
@Mixin(Block.class)
public abstract class BlockOrbApiMixin {

    @WrapOperation(method = "popExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"))
    private void fusioncore$blockOrbs(ServerLevel level, Vec3 pos, int amount, Operation<Void> original) {
        if (FusionCoreExperienceOrbs.hasContext()) {
            original.call(level, pos, amount);
        } else {
            FusionCoreExperienceOrbs.with(ExperienceOrb$SpawnReason.BLOCK_BREAK, null, null, () -> original.call(level, pos, amount));
        }
    }
}
