package io.izzel.arclight.common.mixin.core.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GrowingPlantHeadBlock.class)
public class GrowingPlantHeadBlockMixin {

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean arclight$blockGrow(ServerLevel world, BlockPos to, BlockState state, BlockState state1, ServerLevel worldIn, BlockPos from, RandomSource random) {
        return CraftEventFactory.handleBlockSpreadEvent(world, from, to, state);
    }

    // Fusion Core: Spigot's growth.{kelp,twisting-vines,weeping-vines,cave-vines}-modifier scale the grow chance
    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method = "randomTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/GrowingPlantHeadBlock;growPerTickProbability:D"))
    private double fusioncore$growthModifier(double probability, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) ServerLevel level) {
        return io.izzel.arclight.common.mod.util.FusionCoreGrowth.modifier(this, level) / 100.0D * probability;
    }
}
