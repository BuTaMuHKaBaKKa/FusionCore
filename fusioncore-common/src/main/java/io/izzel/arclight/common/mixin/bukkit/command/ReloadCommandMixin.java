package io.izzel.arclight.common.mixin.bukkit.command;

import io.izzel.arclight.common.bridge.bukkit.CraftServerBridge;
import io.izzel.arclight.common.mod.mixins.annotation.TransformAccess;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.command.defaults.ReloadCommand;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Fusion Core: Paper's {@code /reload}: {@code confirm}, {@code permissions}, {@code commands}, and the lifecycle lock. */
@Mixin(value = ReloadCommand.class, remap = false)
public abstract class ReloadCommandMixin extends BukkitCommand {

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)
    private static final String RELOADING_DISABLED_MESSAGE = "A lifecycle event handler has been registered which makes reloading plugins not possible";

    protected ReloadCommandMixin(String name) {
        super(name);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fusioncore$usage(String name, CallbackInfo ci) {
        this.usageMessage = "/reload [permissions|commands|confirm]";
    }

    /**
     * @author Fusion Core
     * @reason Paper's subcommands and confirmation
     */
    @Overwrite
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        boolean confirmed = System.getProperty("LetMeReload") != null;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("permissions")) {
                ((CraftServerBridge) Bukkit.getServer()).fusioncore$reloadPermissions();
                Command.broadcastCommandMessage(sender, ChatColor.GREEN + "Permissions successfully reloaded.");
                return true;
            } else if ("commands".equalsIgnoreCase(args[0])) {
                if (((CraftServerBridge) Bukkit.getServer()).fusioncore$reloadCommandAliases()) {
                    Command.broadcastCommandMessage(sender, ChatColor.GREEN + "Command aliases successfully reloaded.");
                } else {
                    Command.broadcastCommandMessage(sender, ChatColor.RED + "An error occurred while trying to reload command aliases.");
                }
                return true;
            } else if ("confirm".equalsIgnoreCase(args[0])) {
                confirmed = true;
            } else {
                Command.broadcastCommandMessage(sender, ChatColor.RED + "Usage: " + this.usageMessage);
                return true;
            }
        }
        if (!confirmed) {
            Command.broadcastCommandMessage(sender, ChatColor.RED + "Are you sure you wish to reload your server? Doing so may cause bugs and memory leaks. "
                + "It is recommended to restart instead of using /reload. To confirm, please type " + ChatColor.YELLOW + "/reload confirm");
            return true;
        }

        Command.broadcastCommandMessage(sender, ChatColor.RED + "Please note that this command is not supported and may cause issues when using some plugins.");
        Command.broadcastCommandMessage(sender, ChatColor.RED + "If you encounter any issues please use the /stop command to restart your server.");
        try {
            Bukkit.reload();
        } catch (IllegalStateException ex) {
            if (RELOADING_DISABLED_MESSAGE.equals(ex.getMessage())) {
                Command.broadcastCommandMessage(sender, ChatColor.RED + RELOADING_DISABLED_MESSAGE);
                return true;
            }
        }
        Command.broadcastCommandMessage(sender, ChatColor.GREEN + "Reload complete.");
        return true;
    }

    /**
     * @author Fusion Core
     * @reason Paper's subcommands
     */
    @Overwrite
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return com.google.common.collect.Lists.newArrayList("permissions", "commands");
    }
}
