package io.izzel.arclight.common.mixin.core.server.level;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: the chunk holders of a world, for Paper's chunk and block entity counts (Paper patch 0123). */
@Mixin(ChunkMap.class)
public interface ChunkMapPaperAccessor {

    @Invoker("getChunks")
    Iterable<ChunkHolder> fusioncore$chunks();
}
