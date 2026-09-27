package io.izzel.arclight.common.mixin.core.server.commands;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.context.CommandContext;
import io.izzel.arclight.common.bridge.core.commands.CommandSourceStackBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.GameRules;
import org.bukkit.GameRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRuleCommand.class)
public class GameRuleCommandMixin {

    @Redirect(method = "setRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getGameRules()Lnet/minecraft/world/level/GameRules;"))
    private static GameRules arclight$perWorldGameRule(MinecraftServer minecraftServer, CommandContext<CommandSourceStack> context) {
        return context.getSource().getLevel().getGameRules();
    }

    @Redirect(method = "queryRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getGameRules()Lnet/minecraft/world/level/GameRules;"))
    private static GameRules arclight$perWorldGameRule2(MinecraftServer minecraftServer, CommandSourceStack source) {
        return source.getLevel().getGameRules();
    }

    /** Fusion Core: Paper's {@code WorldGameRuleChangeEvent} for {@code /gamerule} (boolean and integer rules). */
    @WrapOperation(method = "setRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules$Value;setFromArgument(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;)V"))
    private static void fusioncore$gameRuleEvent(GameRules.Value<?> rule, CommandContext<CommandSourceStack> context, String argument,
                                               Operation<Void> original, CommandContext<CommandSourceStack> outerContext, GameRules.Key<?> key) {
        if (!(rule instanceof GameRules.BooleanValue) && !(rule instanceof GameRules.IntegerValue)) {
            original.call(rule, context, argument);
            return;
        }
        CommandSourceStack source = context.getSource();
        String requested = String.valueOf(context.getArgument(argument, Object.class));
        WorldGameRuleChangeEvent event = new WorldGameRuleChangeEvent(((WorldBridge) source.getLevel()).bridge$getWorld(),
            ((CommandSourceStackBridge) source).bridge$getBukkitSender(), GameRule.getByName(key.toString()), requested);
        if (!io.izzel.arclight.common.mod.util.FusionCoreEvents.call(event)) {
            rule.onChanged(source.getServer());
            return;
        }
        if (event.getValue().equals(requested)) {
            original.call(rule, context, argument);
            return;
        }
        rule.deserialize(event.getValue());
        rule.onChanged(source.getServer());
    }
}
