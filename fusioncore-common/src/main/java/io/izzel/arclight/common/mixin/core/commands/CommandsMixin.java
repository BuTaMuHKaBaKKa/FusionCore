package io.izzel.arclight.common.mixin.core.commands;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import com.google.common.collect.Maps;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.izzel.arclight.common.bridge.core.commands.CommandsBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.bridge.core.server.MinecraftServerBridge;
import io.izzel.arclight.common.mod.compat.CommandNodeHooks;
import io.izzel.arclight.common.mod.mixins.annotation.CreateConstructor;
import io.izzel.arclight.common.mod.util.BukkitDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.spigotmc.SpigotConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;

@Mixin(Commands.class)
public abstract class CommandsMixin implements CommandsBridge {

    // @formatter:off
    @Shadow public abstract void performCommand(ParseResults<CommandSourceStack> p_242844_, String p_242841_);
    @Shadow public abstract void performPrefixedCommand(CommandSourceStack p_230958_, String p_230959_);
    @Mutable @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;
    @Shadow protected abstract void fillUsableCommands(CommandNode<CommandSourceStack> rootCommandSource, CommandNode<SharedSuggestionProvider> rootSuggestion, CommandSourceStack source, Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> commandNodeToSuggestionNode);
    // @formatter:on

    @CreateConstructor
    public void arclight$constructor() {
        this.dispatcher = new BukkitDispatcher((Commands) (Object) this);
        this.dispatcher.setConsumer(ExecutionCommandSource.resultConsumer());
    }

    public void performPrefixedCommand(CommandSourceStack commandSourceStack, String s, String label) {
        this.performPrefixedCommand(commandSourceStack, s);
    }

    public void performCommand(ParseResults<CommandSourceStack> parseResults, String s, String label) {
        this.performCommand(parseResults, s);
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public void sendCommands(ServerPlayer player) {
        if (SpigotConfig.tabComplete < 0) return;
        Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> map = Maps.newIdentityHashMap();

        RootCommandNode<SharedSuggestionProvider> vanillaRoot = new RootCommandNode<>();
        Commands vanillaCommands = ((MinecraftServerBridge) player.server).bridge$getVanillaCommands();
        map.put(vanillaCommands.getDispatcher().getRoot(), vanillaRoot);

        // FORGE: Use our own command node merging method to handle redirect nodes properly, see issue #7551
        bridge$forge$mergeNode(vanillaCommands.getDispatcher().getRoot(), vanillaRoot, map, player.createCommandSourceStack(), ctx -> 0, suggest -> SuggestionProviders.safelySwap((com.mojang.brigadier.suggestion.SuggestionProvider<SharedSuggestionProvider>) (com.mojang.brigadier.suggestion.SuggestionProvider<?>) suggest));

        RootCommandNode<SharedSuggestionProvider> node = new RootCommandNode<>();
        map.put(this.dispatcher.getRoot(), node);
        // Fusion Core: Paper's clientNode (patch 0276): a raw command from CommandRegisteredEvent sends the plugin's node in place of
        // the server's one (NeoForge merges the tree with its own CommandHelper, so the swap is done on the root's children here)
        CommandNode<CommandSourceStack> serverRoot = io.izzel.arclight.common.mod.util.FusionCoreBrigadier.clientRoot(this.dispatcher.getRoot());
        map.put(serverRoot, node);
        bridge$forge$mergeNode(serverRoot, node, map, player.createCommandSourceStack(), ctx -> 0, suggest -> SuggestionProviders.safelySwap((com.mojang.brigadier.suggestion.SuggestionProvider<SharedSuggestionProvider>) (com.mojang.brigadier.suggestion.SuggestionProvider<?>) suggest));

        // Fusion Core: Paper's AsyncPlayerSendCommandsEvent, before PlayerCommandSendEvent as in Paper; the tree is
        // built on the main thread here, so it fires once, synchronously, with hasFiredAsync = false
        Bukkit.getPluginManager().callEvent(new AsyncPlayerSendCommandsEvent<>(((ServerPlayerBridge) player).bridge$getBukkitEntity(), (RootCommandNode) node, false));

        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (CommandNode<SharedSuggestionProvider> child : node.getChildren()) {
            set.add(child.getName());
        }
        PlayerCommandSendEvent event = new PlayerCommandSendEvent(((ServerPlayerBridge) player).bridge$getBukkitEntity(), new LinkedHashSet<>(set));
        Bukkit.getPluginManager().callEvent(event);
        for (String s : set) {
            if (!event.getCommands().contains(s)) {
                CommandNodeHooks.removeCommand(node, s);
            }
        }
        player.connection.send(new ClientboundCommandsPacket(node));
    }

    @Redirect(method = "fillUsableCommands", at = @At(value = "INVOKE", remap = false, target = "Lcom/mojang/brigadier/tree/CommandNode;canUse(Ljava/lang/Object;)Z"))
    private <S> boolean arclight$canUse(CommandNode<S> commandNode, S source) {
        return CommandNodeHooks.canUse(commandNode, source);
    }

    @Override
    public <S, T> void bridge$forge$mergeNode(CommandNode<S> sourceNode, CommandNode<T> resultNode,
                                              Map<CommandNode<S>, CommandNode<T>> sourceToResult,
                                              S canUse, Command<T> execute,
                                              Function<SuggestionProvider<S>, SuggestionProvider<T>> sourceToResultSuggestion) {
        fillUsableCommands((CommandNode<CommandSourceStack>) sourceNode,
                (CommandNode<SharedSuggestionProvider>) resultNode,
                (CommandSourceStack) canUse,
                (Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>>) (Map<?, ?>) sourceToResult);
    }

    // Fusion Core: Paper's UnknownCommandEvent (Paper patch 0137) for a command that fails to parse: vanilla's two failure
    // lines become the event's message (null when Spigot's unknown-command message is off), sent if a listener leaves one

    @org.spongepowered.asm.mixin.Unique private static final ThreadLocal<java.util.List<net.minecraft.network.chat.Component>> fusioncore$FAILURE = new ThreadLocal<>();

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "finishParsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandSourceStack;sendFailure(Lnet/minecraft/network/chat/Component;)V"))
    private static void fusioncore$collectFailure(CommandSourceStack source, net.minecraft.network.chat.Component message, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        java.util.List<net.minecraft.network.chat.Component> lines = fusioncore$FAILURE.get();
        if (lines == null) {
            fusioncore$FAILURE.set(lines = new java.util.ArrayList<>());
        }
        lines.add(message);
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "finishParsing", at = @At("RETURN"))
    private static void fusioncore$unknownCommand(com.mojang.brigadier.ParseResults<CommandSourceStack> results, String command, CommandSourceStack source,
                                                org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<?> cir) {
        java.util.List<net.minecraft.network.chat.Component> lines = fusioncore$FAILURE.get();
        fusioncore$FAILURE.remove();
        if (lines == null || lines.isEmpty()) {
            return;
        }
        net.minecraft.network.chat.MutableComponent message = net.minecraft.network.chat.Component.empty().withStyle(net.minecraft.ChatFormatting.RED);
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                message.append("\n");
            }
            message.append(lines.get(i));
        }
        org.bukkit.event.command.UnknownCommandEvent event = new org.bukkit.event.command.UnknownCommandEvent(
            ((io.izzel.arclight.common.bridge.core.commands.CommandSourceStackBridge) source).bridge$getBukkitSender(), command,
            org.spigotmc.SpigotConfig.unknownCommandMessage.isEmpty() ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.asAdventure(message, source.registryAccess()));
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.message() != null) {
            source.sendFailure(io.izzel.arclight.common.mod.util.FusionCoreAdventure.asVanilla(event.message(), source.registryAccess()));
        }
    }
}
