package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.block.CraftMovingPiston;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@link io.papermc.paper.block.MovingPiston} (patch 0757), read from the live block entity as Paper does. */
@Mixin(value = CraftMovingPiston.class, remap = false)
public abstract class CraftMovingPistonMixin implements io.papermc.paper.block.MovingPiston {

    private PistonMovingBlockEntity fusioncore$piston() {
        return (PistonMovingBlockEntity) ((CraftBlockEntityStateAccessor) this).arclight$getBlockEntity();
    }

    @Override
    public BlockData getMovingBlock() {
        return CraftBlockData.fromData(this.fusioncore$piston().getMovedState());
    }

    @Override
    public BlockFace getDirection() {
        return CraftBlock.notchToBlockFace(this.fusioncore$piston().getDirection());
    }

    @Override
    public boolean isExtending() {
        return this.fusioncore$piston().isExtending();
    }

    @Override
    public boolean isPistonHead() {
        return this.fusioncore$piston().isSourcePiston();
    }
}
