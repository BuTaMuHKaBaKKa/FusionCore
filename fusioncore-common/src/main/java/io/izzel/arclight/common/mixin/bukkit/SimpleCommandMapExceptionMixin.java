package io.izzel.arclight.common.mixin.bukkit;

import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import com.destroystokyo.paper.exception.ServerCommandException;
import com.destroystokyo.paper.exception.ServerTabCompleteException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fusion Core: Paper's ServerExceptionEvent (Paper patch api/0021) for a command or tab completer that threw, just before
 * Bukkit wraps the error in a CommandException.
 */
@Mixin(value = SimpleCommandMap.class, remap = false)
public abstract class SimpleCommandMapExceptionMixin {

    // Fusion Core: Paper's constructor with the map of known commands supplied by the caller (API patch "Brigadier based command API")
    @org.spongepowered.asm.mixin.Mutable @org.spongepowered.asm.mixin.Final @org.spongepowered.asm.mixin.Shadow protected java.util.Map<String, Command> knownCommands;
    @org.spongepowered.asm.mixin.Mutable @org.spongepowered.asm.mixin.Final @org.spongepowered.asm.mixin.Shadow private org.bukkit.Server server;

    @org.spongepowered.asm.mixin.Shadow
    private void setDefaultCommands() {
    }

    @io.izzel.arclight.common.mod.mixins.annotation.CreateConstructor
    public void fusioncore$constructor(org.bukkit.Server server, java.util.Map<String, Command> backing) {
        this.knownCommands = backing;
        this.server = server;
        this.setDefaultCommands();
    }

    @Inject(method = "dispatch", at = @At(value = "NEW", target = "org/bukkit/command/CommandException"))
    private void fusioncore$commandException(CommandSender sender, String commandLine, CallbackInfoReturnable<Boolean> cir,
                                           @com.llamalad7.mixinextras.sugar.Local Command target, @com.llamalad7.mixinextras.sugar.Local String[] args, @com.llamalad7.mixinextras.sugar.Local Throwable thrown) {
        Bukkit.getPluginManager().callEvent(new ServerExceptionEvent(new ServerCommandException(thrown, target, sender, args)));
    }

    @Inject(method = "tabComplete(Lorg/bukkit/command/CommandSender;Ljava/lang/String;Lorg/bukkit/Location;)Ljava/util/List;",
        at = @At(value = "NEW", target = "org/bukkit/command/CommandException"))
    private void fusioncore$tabCompleteException(CommandSender sender, String cmdLine, org.bukkit.Location location, CallbackInfoReturnable<java.util.List<String>> cir,
                                               @com.llamalad7.mixinextras.sugar.Local Command target, @com.llamalad7.mixinextras.sugar.Local String[] args, @com.llamalad7.mixinextras.sugar.Local Throwable thrown) {
        Bukkit.getPluginManager().callEvent(new ServerExceptionEvent(new ServerTabCompleteException(
            "Unhandled exception executing tab-completer for '" + cmdLine + "' in " + target, thrown, target, sender, args)));
    }
}
