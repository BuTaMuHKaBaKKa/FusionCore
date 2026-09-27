package io.izzel.arclight.common.mixin.bukkit;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.minecart.CommandMinecart;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code CommandBlockHolder} on the command block minecart. */
@Mixin(value = CommandMinecart.class, remap = false)
public interface CommandMinecartMixin extends io.papermc.paper.command.CommandBlockHolder {

    // Fusion Core: Paper's CommandBlockHolder

    @Override
    default Component lastOutput() {
        return io.izzel.arclight.common.mod.util.FusionCoreCommandBlocks.lastOutput(this);
    }

    @Override
    default void lastOutput(Component lastOutput) {
        io.izzel.arclight.common.mod.util.FusionCoreCommandBlocks.lastOutput(this, lastOutput);
    }

    @Override
    default int getSuccessCount() {
        return io.izzel.arclight.common.mod.util.FusionCoreCommandBlocks.getSuccessCount(this);
    }

    @Override
    default void setSuccessCount(int successCount) {
        io.izzel.arclight.common.mod.util.FusionCoreCommandBlocks.setSuccessCount(this, successCount);
    }
}
