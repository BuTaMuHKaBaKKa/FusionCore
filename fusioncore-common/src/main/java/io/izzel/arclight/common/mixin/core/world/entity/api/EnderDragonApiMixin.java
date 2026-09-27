package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.world.entity.api.EnderDragonApiBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fusion Core: Paper's changeable dragon podium (patch 0687). Every place that computes the podium from the fight origin asks
 * {@link #fusioncore$getPodium()} instead; the phases do the same in {@link DragonPhasePodiumApiMixin}.
 */
@Mixin(EnderDragon.class)
public abstract class EnderDragonApiMixin implements EnderDragonApiBridge {

    @Shadow public abstract BlockPos getFightOrigin();

    @Unique private @Nullable BlockPos fusioncore$podium;

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/EndPodiumFeature;getLocation(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos fusioncore$podium(BlockPos origin, Operation<BlockPos> original) {
        return this.fusioncore$podium != null ? this.fusioncore$podium : original.call(origin);
    }

    @Override
    public BlockPos fusioncore$getPodium() {
        return this.fusioncore$podium != null ? this.fusioncore$podium : EndPodiumFeature.getLocation(this.getFightOrigin());
    }

    @Override
    public @Nullable BlockPos fusioncore$customPodium() {
        return this.fusioncore$podium;
    }

    @Override
    public void fusioncore$setPodium(@Nullable BlockPos podium) {
        this.fusioncore$podium = podium;
    }

    // Fusion Core: Paper's orb reason for the dragon's death experience (patch 0125)
    @WrapOperation(method = "tickDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"))
    private void fusioncore$deathOrbs(net.minecraft.server.level.ServerLevel level, net.minecraft.world.phys.Vec3 pos, int amount, Operation<Void> original) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        io.izzel.arclight.common.mod.util.FusionCoreExperienceOrbs.with(org.bukkit.entity.ExperienceOrb$SpawnReason.ENTITY_DEATH, dragon.lastHurtByPlayer, dragon,
            () -> original.call(level, pos, amount));
    }
}
