package io.izzel.arclight.common.mixin.core.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.izzel.arclight.common.mod.util.FusionCoreGrowth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Fusion Core: Spigot's growth.*-modifier for the blocks whose random tick grows on random.nextInt(n) == 0
@Mixin({CropBlock.class, MushroomBlock.class, NetherWartBlock.class, SaplingBlock.class, CocoaBlock.class, BambooStalkBlock.class,
    BambooSaplingBlock.class, SweetBerryBushBlock.class, StemBlock.class})
public abstract class GrowthModifierMixin {

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private int fusioncore$growthModifier(RandomSource random, int bound, Operation<Integer> original, @Local(argsOnly = true) ServerLevel level) {
        return FusionCoreGrowth.roll(random, bound, FusionCoreGrowth.modifier(this, level), original);
    }
}
