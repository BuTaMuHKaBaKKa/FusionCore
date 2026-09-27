package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreTypes;
import org.bukkit.block.BlockType;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code BlockType#hasCollision} (patch 0590); {@code FeatureDependant} (patch "Add FeatureFlag API"). */
@Mixin(value = BlockType.class, remap = false)
public interface BlockTypeMixin extends io.papermc.paper.world.flag.FeatureDependant {

    default boolean hasCollision() {
        return FusionCoreTypes.hasCollision((BlockType) this);
    }
}
