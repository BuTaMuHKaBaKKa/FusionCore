package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.world.level.entity.PersistentEntitySectionManagerBridge;
import io.izzel.arclight.common.mod.ArclightConstants;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.bukkit.craftbukkit.v.CraftChunk;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Mixin(value = CraftChunk.class, remap = false)
public abstract class CraftChunkMixin {
    @Shadow
    public abstract CraftWorld getCraftWorld();

    @Shadow
    @Final
    private int x;

    @Shadow
    @Final
    private int z;

    /**
     * @author InitAuther97
     * @reason mimic Paper chunk system behavior: don't return entities for unloaded or invalid entities
     *         Here we do not return entities if entities are not yet loaded. So if load event is fired,
     *         unload event will be fired; If load event is not fired, then neither is unload event.
     */
    @Overwrite
    public Entity[] getEntities() {
        PersistentEntitySectionManager<net.minecraft.world.entity.Entity> entityManager = this.getCraftWorld().getHandle().entityManager;
        long pair = ChunkPos.asLong(this.x, this.z);
        if (!entityManager.areEntitiesLoaded(pair)) {
            return ArclightConstants.EMPTY_ENTITIES;
        }
        final var bridge = ((PersistentEntitySectionManagerBridge) entityManager);
        final var sectionStorage = (EntitySectionStorage<net.minecraft.world.entity.Entity>) bridge.getSectionStorage();
        return sectionStorage.getExistingSectionsInChunk(ChunkPos.asLong(this.x, this.z))
                .flatMap(EntitySection::getEntities)
                .map(net.minecraft.world.entity.Entity::bridge$getBukkitEntity)
                .filter(Objects::nonNull)
                .toArray(Entity[]::new);
    }

    @Shadow
    public abstract ChunkAccess getHandle(ChunkStatus chunkStatus);

    @Shadow
    public abstract boolean isLoaded();

    /** Fusion Core: tests the predicate before building a block state. */
    public Collection<BlockState> getTileEntities(Predicate<? super Block> blockPredicate, boolean useSnapshot) {
        Preconditions.checkNotNull(blockPredicate, "blockPredicate");
        if (!this.isLoaded()) {
            this.getCraftWorld().getChunkAt(this.x, this.z); // Transient load for this tick
        }
        ChunkAccess chunk = this.getHandle(ChunkStatus.FULL);
        CraftWorld world = this.getCraftWorld();
        List<BlockState> result = new ArrayList<>();
        for (BlockPos pos : new ArrayList<>(chunk.blockEntities.keySet())) {
            Block block = world.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
            if (blockPredicate.test(block)) {
                result.add(block.getState());
            }
        }
        return result;
    }
}
