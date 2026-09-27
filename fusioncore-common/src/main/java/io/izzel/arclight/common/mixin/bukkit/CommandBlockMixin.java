package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.block.CommandBlock;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code name()}/{@code name(Component)} on {@link CommandBlock} (Paper patch 0006 "Adventure"). */
@Mixin(value = CommandBlock.class, remap = false)
public interface CommandBlockMixin extends io.papermc.paper.command.CommandBlockHolder { // Fusion Core: as in Paper

    default Component name() {
        return FusionCoreAdventure.fromLegacy(((CommandBlock) this).getName());
    }

    default void name(Component name) {
        ((CommandBlock) this).setName(name == null ? null : FusionCoreAdventure.toLegacyRgb(name));
    }

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
