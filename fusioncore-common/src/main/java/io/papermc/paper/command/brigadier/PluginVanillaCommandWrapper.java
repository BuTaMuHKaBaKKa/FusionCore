package io.papermc.paper.command.brigadier;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.minecraft.commands.Commands;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.craftbukkit.v.command.VanillaCommandWrapper;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Fusion Core: the command map entry of a command registered through Paper's {@link Commands} (patch 0965). */
public class PluginVanillaCommandWrapper extends Command implements PluginIdentifiableCommand {

    /** The plugin's node under its namespaced label ({@code plugin:command}); other labels get copies of it. */
    public final CommandNode<net.minecraft.commands.CommandSourceStack> vanillaCommand;
    private final Plugin plugin;
    private List<String> aliases;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PluginVanillaCommandWrapper(String name, String description, List<String> aliases, CommandNode<CommandSourceStack> vanillaCommand, Plugin plugin) {
        super(name, description == null ? "" : description, usage(name, (CommandNode) vanillaCommand), aliases);
        this.vanillaCommand = (CommandNode) vanillaCommand;
        this.plugin = plugin;
        this.aliases = aliases;
    }

    // Paper's usage: the command, then each smart-usage branch on its own line (seen with the console's rights here, Paper
    // uses a dummy source with permission level 4)
    private static String usage(String name, CommandNode<net.minecraft.commands.CommandSourceStack> node) {
        String base = "/" + name;
        try {
            CommandNode<net.minecraft.commands.CommandSourceStack> target = node.getRedirect() != null ? node.getRedirect() : node;
            Map<CommandNode<net.minecraft.commands.CommandSourceStack>, String> map = new CommandDispatcher<net.minecraft.commands.CommandSourceStack>()
                .getSmartUsage(target, ArclightServer.getMinecraftServer().createCommandSourceStack());
            return map.isEmpty() ? base : base + " " + String.join("\n" + base + " ", map.values());
        } catch (Throwable t) {
            return base;
        }
    }

    private static Commands commands() {
        return ArclightServer.getMinecraftServer().getCommands();
    }

    private String toDispatcher(String[] args) {
        return this.vanillaCommand.getName() + (args.length > 0 ? " " + Joiner.on(' ').join(args) : "");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }
        commands().performPrefixedCommand(VanillaCommandWrapper.getListener(sender), this.toDispatcher(args));
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, Location location) throws IllegalArgumentException {
        Preconditions.checkArgument(sender != null, "Sender cannot be null");
        Preconditions.checkArgument(args != null, "Arguments cannot be null");
        Preconditions.checkArgument(alias != null, "Alias cannot be null");
        CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher = commands().getDispatcher();
        ParseResults<net.minecraft.commands.CommandSourceStack> parsed = dispatcher.parse(this.toDispatcher(args), VanillaCommandWrapper.getListener(sender));
        List<String> results = new ArrayList<>();
        dispatcher.getCompletionSuggestions(parsed).thenAccept(suggestions -> suggestions.getList().forEach(s -> results.add(s.getText())));
        return results;
    }

    @Override
    public @NotNull List<String> getAliases() {
        return this.aliases;
    }

    @Override
    public @NotNull Command setAliases(@NotNull List<String> aliases) {
        return this;
    }

    void setRegisteredAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return this.plugin;
    }

    // Paper: shown in the help menu
    @Override
    public boolean isRegistered() {
        return true;
    }
}
