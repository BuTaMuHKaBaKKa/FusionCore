package io.izzel.arclight.common.mixin.bukkit.event;

import org.bukkit.block.data.BlockData;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code BlockPhysicsEvent#getChangedBlockData} (Paper API patch 0138). */
@Mixin(value = BlockPhysicsEvent.class, remap = false)
public abstract class BlockPhysicsEventMixin {

    @Shadow @Final private BlockData changed;

    public BlockData getChangedBlockData() {
        return this.changed.clone();
    }
}
