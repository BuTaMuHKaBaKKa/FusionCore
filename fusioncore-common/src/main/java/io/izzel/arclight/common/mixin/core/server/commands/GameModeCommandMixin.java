package io.izzel.arclight.common.mixin.core.server.commands;

import com.mojang.brigadier.context.CommandContext;
import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.bukkit.event.player.PlayerGameModeChangeEvent$Cause;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * Fusion Core: a game mode change from {@code /gamemode} carries Paper's {@code Cause.COMMAND}. The cause is
 * set before each player's change, since {@code setMode} changes a whole list of players.
 */
@Mixin(GameModeCommand.class)
public class GameModeCommandMixin {

    @Inject(method = "setMode", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setGameMode(Lnet/minecraft/world/level/GameType;)Z"))
    private static void fusioncore$commandCause(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, GameType gameType,
                                              CallbackInfoReturnable<Integer> cir) {
        FusionCoreEvents.clearGameModeCause();
        FusionCoreEvents.gameModeCause(PlayerGameModeChangeEvent$Cause.COMMAND, null);
    }

    @Inject(method = "setMode", at = @At("RETURN"))
    private static void fusioncore$commandCauseDone(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, GameType gameType,
                                                  CallbackInfoReturnable<Integer> cir) {
        FusionCoreEvents.clearGameModeCause();
    }
}
