package io.izzel.arclight.common.mod.util;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import org.spigotmc.SpigotWorldConfig;

/**
 * Spigot's growth modifiers ({@code growth.*-modifier} in spigot.yml) for the blocks whose random tick is a
 * {@code random.nextInt(n) == 0} roll or an age counter. Spigot replaces the roll with
 * {@code random.nextFloat() < modifier / (100 * n)} (SPIGOT-7159); with the default 100 the vanilla roll is kept.
 */
public final class FusionCoreGrowth {

    private FusionCoreGrowth() {
    }

    public static int modifier(Object block, Level level) {
        SpigotWorldConfig config = ((WorldBridge) level).bridge$spigotConfig();
        if (block instanceof CropBlock) {
            if (block == Blocks.BEETROOTS) {
                return config.beetrootModifier;
            } else if (block == Blocks.CARROTS) {
                return config.carrotModifier;
            } else if (block == Blocks.POTATOES) {
                return config.potatoModifier;
            }
            return config.wheatModifier;
        } else if (block instanceof MushroomBlock) {
            return config.mushroomModifier;
        } else if (block instanceof NetherWartBlock) {
            return config.wartModifier;
        } else if (block instanceof SaplingBlock) {
            return config.saplingModifier;
        } else if (block instanceof CocoaBlock) {
            return config.cocoaModifier;
        } else if (block instanceof BambooStalkBlock || block instanceof BambooSaplingBlock) {
            return config.bambooModifier;
        } else if (block instanceof SweetBerryBushBlock) {
            return config.sweetBerryModifier;
        } else if (block instanceof StemBlock) {
            return block == Blocks.PUMPKIN_STEM ? config.pumpkinModifier : config.melonModifier;
        } else if (block instanceof CactusBlock) {
            return config.cactusModifier;
        } else if (block instanceof SugarCaneBlock) {
            return config.caneModifier;
        } else if (block == Blocks.KELP) {
            return config.kelpModifier;
        } else if (block == Blocks.TWISTING_VINES) {
            return config.twistingVinesModifier;
        } else if (block == Blocks.WEEPING_VINES) {
            return config.weepingVinesModifier;
        }
        return config.caveVinesModifier;
    }

    /** The {@code random.nextInt(bound)} of a growth roll: 0 means the plant grows. */
    public static int roll(RandomSource random, int bound, int modifier, Operation<Integer> original) {
        if (modifier == 100) {
            return original.call(random, bound);
        }
        return random.nextFloat() < modifier / (100.0F * bound) ? 0 : 1;
    }

    /**
     * Spigot's age step of cactus and sugar cane: the plant grows at age 15 or on a {@code modifier / 1600} roll, else
     * the age goes up on a second roll of the same chance. Returns the age the vanilla code should see: 15 makes it
     * grow, {@code age} makes it age, {@code age - 1} leaves the block as it is.
     */
    public static int ageStep(RandomSource random, int age, int modifier) {
        if (modifier == 100) {
            return age;
        }
        float chance = modifier / (100.0F * 16);
        if (age >= 15 || random.nextFloat() < chance) {
            return 15;
        } else if (random.nextFloat() < chance) {
            return age;
        }
        return age - 1;
    }
}
