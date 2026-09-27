package io.papermc.paper.block.fluid;

import com.google.common.base.Preconditions;
import io.papermc.paper.block.fluid.type.PaperFallingFluidData;
import io.papermc.paper.block.fluid.type.PaperFlowingFluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.bukkit.Fluid;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.CraftFluid;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/** Fusion Core: Paper's {@code PaperFluidData} (patch 0929), for every {@link FlowingFluid}, mod fluids included. */
public class PaperFluidData implements FluidData {

    private final FluidState state;

    protected PaperFluidData(final FluidState state) {
        this.state = state;
    }

    public FluidState getState() {
        return this.state;
    }

    @Override
    public final @NotNull Fluid getFluidType() {
        return CraftFluid.minecraftToBukkit(this.state.getType());
    }

    @Override
    public @NotNull PaperFluidData clone() {
        try {
            return (PaperFluidData) super.clone();
        } catch (final CloneNotSupportedException ex) {
            throw new AssertionError("Clone not supported", ex);
        }
    }

    @Override
    public @NotNull Vector computeFlowDirection(final Location location) {
        Preconditions.checkArgument(location.getWorld() != null, "Cannot compute flow direction on world-less location");
        net.minecraft.world.phys.Vec3 flow = this.state.getFlow(((CraftWorld) location.getWorld()).getHandle(),
            new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return new Vector(flow.x, flow.y, flow.z);
    }

    @Override
    public int getLevel() {
        return this.state.getAmount();
    }

    @Override
    public float computeHeight(@NotNull final Location location) {
        Preconditions.checkArgument(location.getWorld() != null, "Cannot compute height on world-less location");
        return this.state.getHeight(((CraftWorld) location.getWorld()).getHandle(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    @Override
    public boolean isSource() {
        return this.state.isSource();
    }

    @Override
    public int hashCode() {
        return this.state.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof final PaperFluidData paperFluidData && this.state.equals(paperFluidData.state);
    }

    @Override
    public String toString() {
        return "PaperFluidData{" + this.state + "}";
    }

    public static PaperFluidData createData(final FluidState state) {
        if (state.getType() instanceof FlowingFluid && state.hasProperty(FlowingFluid.FALLING)) {
            return state.isSource() ? new PaperFallingFluidData(state) : new PaperFlowingFluidData(state);
        }
        return new PaperFluidData(state);
    }
}
