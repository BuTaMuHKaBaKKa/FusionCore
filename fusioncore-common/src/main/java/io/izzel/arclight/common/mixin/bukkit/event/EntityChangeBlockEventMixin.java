package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.EntityEventExtrasBridge;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: the new block data of Paper's {@code EntityBreakDoorEvent} constructor (API patch 0343), set by {@code FusionCoreEvents}. */
@Mixin(value = EntityChangeBlockEvent.class, remap = false)
public abstract class EntityChangeBlockEventMixin implements EntityEventExtrasBridge.ChangeBlock {

    @Shadow @Final @Mutable private BlockData to;

    @Override
    public void bridge$setBlockData(BlockData to) {
        this.to = to;
    }
}
