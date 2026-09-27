package io.izzel.arclight.common.mixin.bukkit.event;

import com.mojang.datafixers.util.Either;
import io.izzel.arclight.common.mod.util.FusionCoreEnumConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Fusion Core: Paper's {@code BedEnterResult.OBSTRUCTED} (patch "Add OBSTRUCTED reason to BedEnterResult"): vanilla's
 * {@code OBSTRUCTED} sleeping problem (a block above the bed) no longer reaches plugins as {@code OTHER_PROBLEM}.
 */
@Mixin(value = CraftEventFactory.class, remap = false)
public abstract class CraftEventFactoryBedMixin {

    @ModifyVariable(method = "callPlayerBedEnterEvent", at = @At("STORE"), ordinal = 0)
    private static PlayerBedEnterEvent.BedEnterResult fusioncore$obstructed(PlayerBedEnterEvent.BedEnterResult result, Player player, BlockPos bed,
                                                                         Either<Player.BedSleepingProblem, Unit> nmsBedResult) {
        if (nmsBedResult.left().orElse(null) == Player.BedSleepingProblem.OBSTRUCTED) {
            return FusionCoreEnumConstants.BedEnterResult$OBSTRUCTED();
        }
        return result;
    }
}
