package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.block.TileState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code TileState#isSnapshot} (patch 0160). Fusion Core's block states are always snapshots
 * ({@code Block#getState(false)} accepts the flag but copies, see BlockMixin), so this is always true.
 */
@Mixin(value = TileState.class, remap = false)
public interface TileStateMixin {

    default boolean isSnapshot() {
        return true;
    }
}
