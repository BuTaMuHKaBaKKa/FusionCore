package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.bukkit.block.EnderChest;
import org.bukkit.craftbukkit.v.block.CraftBlockState;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code EnderChest#isBlocked} (patch 0963): a solid block above, as vanilla checks on use. */
@Mixin(value = EnderChest.class, remap = false)
public interface EnderChestMixin {

    default boolean isBlocked() {
        return FusionCoreBlockStates.enderChestBlocked((CraftBlockState) (Object) this);
    }
}
