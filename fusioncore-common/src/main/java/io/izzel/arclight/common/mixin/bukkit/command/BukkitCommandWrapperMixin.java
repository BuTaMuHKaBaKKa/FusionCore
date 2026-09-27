package io.izzel.arclight.common.mixin.bukkit.command;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommand;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.izzel.arclight.common.mod.util.FusionCoreBrigadier;
import io.papermc.paper.command.brigadier.PluginVanillaCommandWrapper;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v.command.BukkitCommandWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fusion Core: commands registered through Paper's {@code Commands} go into the dispatcher as their own nodes; other
 * Bukkit commands fire Paper's {@link CommandRegisteredEvent}. The wrapper is also a {@link BukkitBrigadierCommand}.
 */
@Mixin(value = BukkitCommandWrapper.class, remap = false)
@SuppressWarnings({"rawtypes", "unchecked", "removal"})
public abstract class BukkitCommandWrapperMixin implements BukkitBrigadierCommand {

    @Shadow @Final private Command command;

    @Inject(method = "register", cancellable = true, at = @At("HEAD"))
    private void fusioncore$register(CommandDispatcher<CommandSourceStack> dispatcher, String label, CallbackInfoReturnable<LiteralCommandNode<CommandSourceStack>> cir) {
        if (this.command instanceof PluginVanillaCommandWrapper wrapper) {
            LiteralCommandNode<CommandSourceStack> node = (LiteralCommandNode<CommandSourceStack>) wrapper.vanillaCommand;
            if (!node.getLiteral().equals(label)) {
                LiteralCommandNode<CommandSourceStack> clone = new LiteralCommandNode<>(label, node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());
                for (CommandNode<CommandSourceStack> child : node.getChildren()) {
                    clone.addChild(child);
                }
                node = clone;
            }
            dispatcher.getRoot().addChild(node);
            cir.setReturnValue(node);
            return;
        }
        if (CommandRegisteredEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }
        Object self = this;
        RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
        LiteralCommandNode<CommandSourceStack> literal = LiteralArgumentBuilder.<CommandSourceStack>literal(label)
            .requires((java.util.function.Predicate<CommandSourceStack>) self).executes((com.mojang.brigadier.Command<CommandSourceStack>) self).build();
        LiteralCommandNode<CommandSourceStack> defaultNode = literal;
        ArgumentCommandNode<CommandSourceStack, String> defaultArgs = RequiredArgumentBuilder.<CommandSourceStack, String>argument("args", StringArgumentType.greedyString())
            .suggests((com.mojang.brigadier.suggestion.SuggestionProvider<CommandSourceStack>) self).executes((com.mojang.brigadier.Command<CommandSourceStack>) self).build();
        literal.addChild(defaultArgs);
        CommandRegisteredEvent event = new CommandRegisteredEvent(label, this, this.command, (RootCommandNode) root, (LiteralCommandNode) literal, (ArgumentCommandNode) defaultArgs);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(null);
            return;
        }
        literal = (LiteralCommandNode<CommandSourceStack>) event.getLiteral();
        if (event.isRawCommand()) {
            FusionCoreBrigadier.setClientNode(defaultNode, literal);
            literal = defaultNode;
        }
        root.addChild(literal);
        cir.setReturnValue(literal);
    }
}
