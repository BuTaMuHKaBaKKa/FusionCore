package io.izzel.arclight.neoforge.mod.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.block.CraftBlockType;

/**
 * Fusion Core: the block a machine is about to place, as BlockPlaceEvent#getBlockPlaced shows it. The machine's setBlock is
 * checked before it happens (cancelling afterwards would run the mods' side effects twice), so the world still holds the old
 * block; this view reports the new state at that position. getBlockReplacedState is taken from the world (the old block).
 */
final class PendingPlaceBlock extends CraftBlock {

    private final BlockState state;

    PendingPlaceBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        super(level, pos);
        this.state = state;
    }

    @Override
    public BlockState getNMS() {
        return this.state;
    }

    @Override
    public Material getType() {
        return CraftBlockType.minecraftToBukkit(this.state.getBlock());
    }
}
