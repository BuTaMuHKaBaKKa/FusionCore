// Fusion Core: Paper 1.21.1 server class (patch 0965, Brigadier command API), as in Paper plus copy().
package io.papermc.paper.command.brigadier.argument;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;

import java.util.function.Predicate;

/*
Basically this converts the argument to a different type when parsing.
 */
public class WrappedArgumentCommandNode<NMS, API> extends ArgumentCommandNode<CommandSourceStack, NMS> {

    private final ArgumentType<API> pureArgumentType;

    public WrappedArgumentCommandNode(
        final String name,
        final ArgumentType<API> pureArgumentType,
        final ArgumentType<NMS> nmsNativeType,
        final Command<CommandSourceStack> command,
        final Predicate<CommandSourceStack> requirement,
        final CommandNode<CommandSourceStack> redirect,
        final RedirectModifier<CommandSourceStack> modifier,
        final boolean forks,
        final SuggestionProvider<CommandSourceStack> customSuggestions
    ) {
        super(name, nmsNativeType, command, requirement, redirect, modifier, forks, customSuggestions);
        if (!ArgumentTypeInfos.isClassRecognized(nmsNativeType.getClass())) {
            // Is this argument an NMS argument?
            throw new IllegalArgumentException("Unexpected argument type was passed: " + nmsNativeType.getClass() + ". This should be an NMS type!");
        }

        this.pureArgumentType = pureArgumentType;
    }

    // See ArgumentCommandNode#parse
    @Override
    public void parse(final StringReader reader, final CommandContextBuilder<CommandSourceStack> contextBuilder) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final API result = this.pureArgumentType.parse(reader); // Use the api argument parser
        final ParsedArgument<CommandSourceStack, API> parsed = new ParsedArgument<>(start, reader.getCursor(), result); // Return an API parsed argument instead.

        contextBuilder.withArgument(this.getName(), parsed);
        contextBuilder.withNode(this, parsed.getRange());
    }

    // Fusion Core: the same node without children, for ApiMirrorRootNode (createBuilder() keeps only the native type)
    public WrappedArgumentCommandNode<NMS, API> copy(final CommandNode<CommandSourceStack> redirect) {
        return new WrappedArgumentCommandNode<>(this.getName(), this.pureArgumentType, this.getType(), this.getCommand(), this.getRequirement(),
            redirect, this.getRedirectModifier(), this.isFork(), this.getCustomSuggestions());
    }
}
