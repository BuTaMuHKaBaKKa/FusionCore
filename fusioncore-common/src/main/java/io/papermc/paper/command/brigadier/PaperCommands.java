package io.papermc.paper.command.brigadier;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.izzel.arclight.common.mod.server.ArclightServer;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventRunner;
import io.papermc.paper.plugin.lifecycle.event.registrar.PaperRegistrar;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.minecraft.commands.CommandBuildContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.command.CraftCommandMap;
import org.bukkit.craftbukkit.v.command.VanillaCommandWrapper;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Fusion Core: Paper's registrar behind {@link Commands} (patch 0965). Registered commands go into Bukkit's command map
 * under Paper's labels and precedence; the next sync puts their nodes into the dispatcher.
 */
public class PaperCommands implements Commands, PaperRegistrar<LifecycleEventOwner> {

    public static final PaperCommands INSTANCE = new PaperCommands();

    private @Nullable LifecycleEventOwner currentContext;
    private @Nullable CommandBuildContext buildContext;
    private final CommandDispatcher<CommandSourceStack> mirrorDispatcher = new CommandDispatcher<>(new ApiMirrorRootNode() {
        @Override
        public CommandDispatcher<net.minecraft.commands.CommandSourceStack> getDispatcher() {
            return ArclightServer.getMinecraftServer().getCommands().getDispatcher();
        }

        @Override
        protected void addUnwrapped(CommandNode<net.minecraft.commands.CommandSourceStack> node) {
            super.addUnwrapped(node);
            PaperCommands.this.registerDirect(node);
        }
    });
    private boolean invalid = false;
    private boolean fired = false;

    @Override
    public void setCurrentContext(final @Nullable LifecycleEventOwner context) {
        this.currentContext = context;
    }

    public void setValid() {
        this.invalid = false;
    }

    @Override
    public void invalidate() {
        this.invalid = true;
    }

    // Fusion Core: the context vanilla's commands are built with, from ReloadableServerResourcesMixin (Paper: setDispatcher)
    public void setBuildContext(final CommandBuildContext buildContext) {
        this.buildContext = buildContext;
    }

    public CommandBuildContext getBuildContext() {
        return requireNonNull(this.buildContext, "The command build context is not ready yet");
    }

    @Override
    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        Preconditions.checkState(!this.invalid, "cannot access the dispatcher in this context");
        return this.mirrorDispatcher;
    }

    @Override
    public @Unmodifiable Set<String> register(final LiteralCommandNode<CommandSourceStack> node, final @Nullable String description, final Collection<String> aliases) {
        return this.register(requireNonNull(this.currentContext, "No lifecycle owner context is set").getPluginMeta(), node, description, aliases);
    }

    @Override
    public @Unmodifiable Set<String> register(final PluginMeta pluginMeta, final LiteralCommandNode<CommandSourceStack> node, final @Nullable String description, final Collection<String> aliases) {
        return this.registerWithFlags(pluginMeta, node, description, aliases, Set.of());
    }

    @Override
    public @Unmodifiable Set<String> registerWithFlags(@NotNull final PluginMeta pluginMeta, @NotNull final LiteralCommandNode<CommandSourceStack> node, @Nullable final String description, @NotNull final Collection<String> aliases, @NotNull final Set<CommandRegistrationFlag> flags) {
        Preconditions.checkState(!this.invalid, "cannot register commands in this context");
        final String identifier = pluginMeta.getName().toLowerCase(Locale.ROOT);
        final String literal = node.getLiteral();
        final PluginCommandNode pluginLiteral = new PluginCommandNode(identifier + ":" + literal, pluginMeta, ApiMirrorRootNode.unwrap(node), description);
        final Plugin plugin = requireNonNull(Bukkit.getPluginManager().getPlugin(pluginMeta.getName()), "Plugin " + pluginMeta.getName() + " is not loaded");
        final PluginVanillaCommandWrapper wrapper = new PluginVanillaCommandWrapper(literal, description, Collections.emptyList(), pluginLiteral, plugin);
        final CraftCommandMap commandMap = commandMap();
        final Map<String, Command> known = commandMap.getKnownCommands();

        final Set<String> registeredLabels = new HashSet<>(aliases.size() * 2 + 2);
        known.put(pluginLiteral.getLiteral(), wrapper);
        registeredLabels.add(pluginLiteral.getLiteral());
        known.put(literal, wrapper); // Plugin commands should override vanilla commands
        registeredLabels.add(literal);

        final List<String> registeredAliases = new ArrayList<>(aliases.size() * 2);
        for (final String alias : aliases) {
            if (registerAlias(known, alias, wrapper)) {
                registeredAliases.add(alias);
            }
            if (registerAlias(known, identifier + ":" + alias, wrapper)) {
                registeredAliases.add(identifier + ":" + alias);
            }
        }
        if (!registeredAliases.isEmpty()) {
            pluginLiteral.setAliases(registeredAliases);
            wrapper.setRegisteredAliases(registeredAliases);
        }
        wrapper.register(commandMap);
        registeredLabels.addAll(registeredAliases);
        return Collections.unmodifiableSet(registeredLabels);
    }

    /**
     * Fusion Core: a literal added straight to {@link #getDispatcher()} goes into the command map as a vanilla-style command.
     */
    @SuppressWarnings("unchecked")
    private void registerDirect(CommandNode<net.minecraft.commands.CommandSourceStack> node) {
        if (!(node instanceof LiteralCommandNode<?>)) {
            return;
        }
        final String prefix = this.currentContext == null ? "minecraft" : this.currentContext.getPluginMeta().getName().toLowerCase(Locale.ROOT);
        final net.minecraft.commands.Commands vanilla = ((io.izzel.arclight.common.bridge.core.server.MinecraftServerBridge) ArclightServer.getMinecraftServer()).bridge$getVanillaCommands();
        commandMap().register(prefix, new VanillaCommandWrapper(vanilla, node));
    }

    private static CraftCommandMap commandMap() {
        return (CraftCommandMap) ((CraftServer) Bukkit.getServer()).getCommandMap();
    }

    // Paper: an alias replaces a vanilla command, never another plugin's command
    private static boolean registerAlias(Map<String, Command> known, String alias, PluginVanillaCommandWrapper wrapper) {
        Command existing = known.get(alias);
        if (existing == null || existing instanceof VanillaCommandWrapper) {
            known.put(alias, wrapper);
            return true;
        }
        return false;
    }

    @Override
    public @Unmodifiable Set<String> register(final String label, final @Nullable String description, final Collection<String> aliases, final BasicCommand basicCommand) {
        return this.register(requireNonNull(this.currentContext, "No lifecycle owner context is set").getPluginMeta(), label, description, aliases, basicCommand);
    }

    @Override
    public @Unmodifiable Set<String> register(final PluginMeta pluginMeta, final String label, final @Nullable String description, final Collection<String> aliases, final BasicCommand basicCommand) {
        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(label)
            .requires(stack -> basicCommand.canUse(stack.getSender()))
            .then(
                Commands.argument("args", StringArgumentType.greedyString())
                    .suggests((context, suggestionsBuilder) -> {
                        String[] args = StringUtils.split(suggestionsBuilder.getRemaining());
                        if (suggestionsBuilder.getRemaining().endsWith(" ")) {
                            args = ArrayUtils.add(args, "");
                        }
                        final SuggestionsBuilder offsetSuggestionsBuilder = suggestionsBuilder.createOffset(suggestionsBuilder.getInput().lastIndexOf(' ') + 1);
                        final Collection<String> suggestions = basicCommand.suggest(context.getSource(), args);
                        suggestions.forEach(offsetSuggestionsBuilder::suggest);
                        return offsetSuggestionsBuilder.buildFuture();
                    })
                    .executes((stack) -> {
                        basicCommand.execute(stack.getSource(), StringUtils.split(stack.getArgument("args", String.class), ' '));
                        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                    })
            )
            .executes((stack) -> {
                basicCommand.execute(stack.getSource(), new String[0]);
                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
            });
        return this.register(pluginMeta, builder.build(), description, aliases);
    }

    // Fusion Core: where Paper fires LifecycleEvents.COMMANDS for plugins

    /**
     * Fires {@link LifecycleEvents#COMMANDS} for plugins at startup (and after Bukkit's {@code /reload}), from
     * {@code CraftServer#enablePlugins(POSTWORLD)} before the help map and the dispatcher are rebuilt from the command map.
     */
    public void fireInitial() {
        this.fire(ReloadableRegistrarEvent.Cause.INITIAL);
        this.fired = true;
    }

    /**
     * Fires {@link LifecycleEvents#COMMANDS} again after the data packs were reloaded, as Paper does: the plugin commands
     * of the previous round leave the command map first. Nothing happens before the startup round.
     */
    public void fireReload() {
        if (!this.fired) {
            return;
        }
        commandMap().getKnownCommands().values().removeIf(command -> command instanceof PluginVanillaCommandWrapper);
        this.fire(ReloadableRegistrarEvent.Cause.RELOAD);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void fire(ReloadableRegistrarEvent.Cause cause) {
        this.setValid();
        // bootstraps first, as with Paper, where their round runs when the data packs load
        LifecycleEventRunner.INSTANCE.callReloadableRegistrarEvent(LifecycleEvents.COMMANDS, this, (Class) io.papermc.paper.plugin.bootstrap.BootstrapContext.class, cause);
        this.setValid();
        // javac does not see Plugin as a LifecycleEventOwner (a mixin adds the supertype)
        LifecycleEventRunner.INSTANCE.callReloadableRegistrarEvent(LifecycleEvents.COMMANDS, this, (Class) Plugin.class, cause);
    }
}
