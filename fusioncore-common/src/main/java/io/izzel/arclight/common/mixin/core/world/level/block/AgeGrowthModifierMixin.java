package io.izzel.arclight.common.mixin.core.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.izzel.arclight.common.mod.util.FusionCoreGrowth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Fusion Core: Spigot's growth.cactus-modifier and growth.cane-modifier, applied to the age the random tick reads
@Mixin({CactusBlock.class, SugarCaneBlock.class})
public abstract class AgeGrowthModifierMixin {

    @ModifyExpressionValue(method = "randomTick", at = @At(value = "INVOKE", target = "Ljava/lang/Integer;intValue()I"))
    private int fusioncore$growthModifier(int age, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) RandomSource random) {
        return FusionCoreGrowth.ageStep(random, age, FusionCoreGrowth.modifier(this, level));
    }
}
