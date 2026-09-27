package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/** Fusion Core: Paper's block-entity queries on {@link Chunk}. Always returns snapshots. */
@Mixin(value = Chunk.class, remap = false)
public interface ChunkMixin {

    default BlockState[] getTileEntities(boolean useSnapshot) {
        return ((Chunk) this).getTileEntities();
    }

    default Collection<BlockState> getTileEntities(Predicate<? super Block> blockPredicate, boolean useSnapshot) {
        List<BlockState> result = new ArrayList<>();
        for (BlockState state : ((Chunk) this).getTileEntities()) {
            if (blockPredicate.test(state.getBlock())) {
                result.add(state);
            }
        }
        return result;
    }

    // Fusion Core: Paper's chunk key and snapshot flag

    default long getChunkKey() {
        return io.izzel.arclight.common.mod.util.FusionCoreChunkStatics.getChunkKey(((Chunk) this).getX(), ((Chunk) this).getZ());
    }

    /** Paper: {@code includeLightData = false} skips light for speed; Spigot's snapshots always carry light, so they are returned. */
    default org.bukkit.ChunkSnapshot getChunkSnapshot(boolean includeMaxblocky, boolean includeBiome, boolean includeBiomeTempRain, boolean includeLightData) {
        return ((Chunk) this).getChunkSnapshot(includeMaxblocky, includeBiome, includeBiomeTempRain);
    }
}
