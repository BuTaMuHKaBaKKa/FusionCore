package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mixin.bukkit.CraftBlockEntityStateAccessor;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import org.bukkit.craftbukkit.v.entity.CraftEntity;

/**
 * Fusion Core: Paper's {@code CommandBlockHolder} bodies for the command block state (its snapshot, applied on {@code update()})
 * and the command block minecart (live).
 */
public final class FusionCoreCommandBlocks {

    private FusionCoreCommandBlocks() {
    }

    private static BaseCommandBlock of(Object holder) {
        if (holder instanceof CraftBlockEntityStateAccessor state) {
            return ((CommandBlockEntity) state.fusioncore$getSnapshot()).getCommandBlock();
        }
        return ((MinecartCommandBlock) ((CraftEntity) holder).getHandle()).getCommandBlock();
    }

    public static Component lastOutput(Object holder) {
        return FusionCoreAdventure.asAdventure(of(holder).getLastOutput());
    }

    public static void lastOutput(Object holder, Component lastOutput) {
        of(holder).setLastOutput(lastOutput == null ? null : FusionCoreAdventure.asVanilla(lastOutput));
    }

    public static int getSuccessCount(Object holder) {
        return of(holder).getSuccessCount();
    }

    public static void setSuccessCount(Object holder, int successCount) {
        of(holder).setSuccessCount(successCount);
    }
}
