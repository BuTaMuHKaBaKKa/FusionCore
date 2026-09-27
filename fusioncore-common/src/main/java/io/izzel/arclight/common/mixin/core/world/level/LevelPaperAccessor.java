package io.izzel.arclight.common.mixin.core.world.level;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Fusion Core: {@code World#getTickableTileEntityCount} (Paper patch 0123). */
@Mixin(Level.class)
public interface LevelPaperAccessor {

    @Accessor("blockEntityTickers")
    List<TickingBlockEntity> fusioncore$blockEntityTickers();
}
