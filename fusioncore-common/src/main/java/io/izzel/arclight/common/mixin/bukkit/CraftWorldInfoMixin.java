package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.bukkit.VanillaBiomeSourceBridge;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.bukkit.craftbukkit.v.generator.CraftWorldInfo;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: the vanilla generator of a world being created, for Paper's {@code vanillaBiomeProvider} (see the bridge). */
@Mixin(value = CraftWorldInfo.class, remap = false)
public abstract class CraftWorldInfoMixin implements VanillaBiomeSourceBridge.WorldInfo {

    private ChunkGenerator fusioncore$vanillaGenerator;
    private RegistryAccess.Frozen fusioncore$registryAccess;

    @Override
    public void fusioncore$setVanillaGenerator(ChunkGenerator generator, RegistryAccess.Frozen registryAccess) {
        this.fusioncore$vanillaGenerator = generator;
        this.fusioncore$registryAccess = registryAccess;
    }

    @Override
    public ChunkGenerator fusioncore$vanillaGenerator() {
        return this.fusioncore$vanillaGenerator;
    }

    @Override
    public RegistryAccess.Frozen fusioncore$registryAccess() {
        return this.fusioncore$registryAccess;
    }

    /** Paper ("Add FeatureFlag API"): the features of the server's world data, which every world shares. */
    public java.util.Set<org.bukkit.FeatureFlag> getFeatureFlags() {
        return io.izzel.arclight.common.mod.util.FusionCoreFeatureFlags.serverFeatures();
    }
}
