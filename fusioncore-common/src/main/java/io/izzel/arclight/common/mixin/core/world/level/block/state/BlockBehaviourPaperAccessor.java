package io.izzel.arclight.common.mixin.core.world.level.block.state;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: {@code Block/BlockState#isCollidable} (Paper patch 0590). */
@Mixin(BlockBehaviour.class)
public interface BlockBehaviourPaperAccessor {

    @Accessor("hasCollision")
    boolean fusioncore$hasCollision();
}
