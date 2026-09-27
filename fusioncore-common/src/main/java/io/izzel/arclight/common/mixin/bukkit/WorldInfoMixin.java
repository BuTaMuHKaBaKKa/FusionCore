package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBiomeProviders;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code WorldInfo#vanillaBiomeProvider} (patch "Expose vanilla BiomeProvider from WorldInfo"). */
@Mixin(value = WorldInfo.class, remap = false)
public interface WorldInfoMixin extends io.papermc.paper.world.flag.FeatureFlagSetHolder { // FeatureFlagSetHolder: Paper

    default BiomeProvider vanillaBiomeProvider() {
        return FusionCoreBiomeProviders.vanillaBiomeProvider((WorldInfo) this);
    }
}
