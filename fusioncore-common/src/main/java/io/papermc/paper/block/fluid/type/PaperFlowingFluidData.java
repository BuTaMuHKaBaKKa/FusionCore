package io.papermc.paper.block.fluid.type;

import net.minecraft.world.level.material.FluidState;

/** Fusion Core: Paper's {@code PaperFlowingFluidData} (Paper patch 0929). */
public class PaperFlowingFluidData extends PaperFallingFluidData implements FlowingFluidData {

    public PaperFlowingFluidData(final FluidState state) {
        super(state);
    }
}
