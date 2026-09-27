package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.bukkit.VanillaBiomeSourceBridge;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.block.CraftBiome;
import org.bukkit.craftbukkit.v.generator.CustomChunkGenerator;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.List;

/** Fusion Core: Paper's {@code WorldInfo#vanillaBiomeProvider}: the biomes the vanilla generator would place. */
public final class FusionCoreBiomeProviders {

    private FusionCoreBiomeProviders() {
    }

    public static BiomeProvider vanillaBiomeProvider(WorldInfo info) {
        if (info instanceof CraftWorld world) {
            return ofWorld(world);
        }
        if (info instanceof VanillaBiomeSourceBridge.WorldInfo bridge && bridge.fusioncore$vanillaGenerator() != null) {
            return ofWorldInfo(info, bridge);
        }
        throw new UnsupportedOperationException("No vanilla generator is known for " + info.getName());
    }

    private static BiomeProvider ofWorld(CraftWorld world) {
        ServerChunkCache serverCache = world.getHandle().getChunkSource();
        ChunkGenerator gen = serverCache.getGenerator();
        BiomeSource biomeSource;
        if (gen instanceof CustomChunkGenerator custom) {
            biomeSource = custom.getDelegate().getBiomeSource();
        } else {
            biomeSource = gen.getBiomeSource();
        }
        if (biomeSource instanceof VanillaBiomeSourceBridge.CustomBiomeSource custom && custom.fusioncore$vanillaBiomeSource() != null) {
            biomeSource = custom.fusioncore$vanillaBiomeSource();
        }
        return provider(biomeSource, serverCache.randomState().sampler());
    }

    private static BiomeProvider ofWorldInfo(WorldInfo info, VanillaBiomeSourceBridge.WorldInfo bridge) {
        ChunkGenerator generator = bridge.fusioncore$vanillaGenerator();
        var noise = bridge.fusioncore$registryAccess().lookupOrThrow(Registries.NOISE);
        RandomState randomState;
        if (generator instanceof NoiseBasedChunkGenerator noiseBased) {
            randomState = RandomState.create(noiseBased.generatorSettings().value(), noise, info.getSeed());
        } else {
            randomState = RandomState.create(NoiseGeneratorSettings.dummy(), noise, info.getSeed());
        }
        return provider(generator.getBiomeSource(), randomState.sampler());
    }

    private static BiomeProvider provider(BiomeSource source, Climate.Sampler sampler) {
        final List<Biome> possibleBiomes = source.possibleBiomes().stream().map(CraftBiome::minecraftHolderToBukkit).toList();
        return new BiomeProvider() {
            @Override
            public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                return CraftBiome.minecraftHolderToBukkit(source.getNoiseBiome(x >> 2, y >> 2, z >> 2, sampler));
            }

            @Override
            public List<Biome> getBiomes(WorldInfo worldInfo) {
                return possibleBiomes;
            }
        };
    }
}
