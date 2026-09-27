package io.izzel.arclight.common.bridge.bukkit;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;

/** Fusion Core: data behind Paper's {@code WorldInfo#vanillaBiomeProvider}. */
public interface VanillaBiomeSourceBridge {

    interface WorldInfo {

        void fusioncore$setVanillaGenerator(ChunkGenerator generator, RegistryAccess.Frozen registryAccess);

        ChunkGenerator fusioncore$vanillaGenerator();

        RegistryAccess.Frozen fusioncore$registryAccess();
    }

    interface CustomBiomeSource {

        void fusioncore$setVanillaBiomeSource(BiomeSource source);

        BiomeSource fusioncore$vanillaBiomeSource();
    }
}
