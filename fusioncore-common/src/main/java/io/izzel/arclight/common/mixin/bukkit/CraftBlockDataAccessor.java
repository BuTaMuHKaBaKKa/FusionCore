package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: CraftBukkit's {@code set(property, value)} for Paper's block data setters (patch 0666). */
@Mixin(value = CraftBlockData.class, remap = false)
public interface CraftBlockDataAccessor {

    @Accessor("state")
    void fusioncore$setState(BlockState state);
}
