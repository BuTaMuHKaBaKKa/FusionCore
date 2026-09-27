package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.bukkit.VanillaBiomeSourceBridge;
import net.minecraft.world.level.biome.BiomeSource;
import org.bukkit.craftbukkit.v.generator.CustomWorldChunkManager;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: the vanilla biome source a plugin's biome provider replaced, for Paper's {@code vanillaBiomeProvider}. */
@Mixin(value = CustomWorldChunkManager.class, remap = false)
public abstract class CustomWorldChunkManagerMixin implements VanillaBiomeSourceBridge.CustomBiomeSource {

    private BiomeSource fusioncore$vanillaBiomeSource;

    @Override
    public void fusioncore$setVanillaBiomeSource(BiomeSource source) {
        this.fusioncore$vanillaBiomeSource = source;
    }

    @Override
    public BiomeSource fusioncore$vanillaBiomeSource() {
        return this.fusioncore$vanillaBiomeSource;
    }
}
