package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.world.entity.api.EnderDragonApiBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonDeathPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoldingPatternPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingApproachPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonTakeoffPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: the dragon phases that fly to the podium use the podium plugins set (Paper patch 0687). */
@Mixin({DragonDeathPhase.class, DragonHoldingPatternPhase.class, DragonLandingApproachPhase.class, DragonLandingPhase.class, DragonTakeoffPhase.class})
public abstract class DragonPhasePodiumApiMixin extends AbstractDragonPhaseInstance {

    private DragonPhasePodiumApiMixin(EnderDragon dragon) {
        super(dragon);
    }

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/EndPodiumFeature;getLocation(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos fusioncore$podium(BlockPos origin, Operation<BlockPos> original) {
        BlockPos podium = ((EnderDragonApiBridge) this.dragon).fusioncore$customPodium();
        return podium != null ? podium : original.call(origin);
    }
}
