package io.papermc.paper.command.brigadier;

import com.google.common.collect.MapMaker;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.VanillaArgumentProviderImpl;
import io.papermc.paper.command.brigadier.argument.WrappedArgumentCommandNode;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Fusion Core: Paper's {@code ApiMirrorRootNode} (patch 0965). Nodes added here are unwrapped and go into the server's
 * dispatcher and command map.
 */
public abstract class ApiMirrorRootNode extends RootCommandNode<CommandSourceStack> {

    private static final Set<Class<? extends ArgumentType<?>>> ARGUMENT_WHITELIST = Set.of(
        BoolArgumentType.class,
        DoubleArgumentType.class,
        FloatArgumentType.class,
        IntegerArgumentType.class,
        LongArgumentType.class,
        StringArgumentType.class
    );

    // Fusion Core: Paper's CommandNode#unwrappedCached
    private static final Map<CommandNode<?>, CommandNode<?>> UNWRAPPED = new MapMaker().weakKeys().makeMap();

    public static void validatePrimitiveType(ArgumentType<?> type) {
        if (ARGUMENT_WHITELIST.contains(type.getClass())) {
            if (!ArgumentTypeInfos.isClassRecognized(type.getClass())) {
                throw new IllegalArgumentException("This whitelisted primitive argument type is not recognized by the server!");
            }
        } else if (!(type instanceof VanillaArgumentProviderImpl.NativeWrapperArgumentType<?, ?> nativeWrapperArgumentType) || !ArgumentTypeInfos.isClassRecognized(nativeWrapperArgumentType.nativeNmsArgumentType().getClass())) {
            throw new IllegalArgumentException("Custom argument type was passed, this was not a recognized type to send to the client! You must only pass vanilla arguments or primitive brig args in the wrapper!");
        }
    }

    public abstract CommandDispatcher<net.minecraft.commands.CommandSourceStack> getDispatcher();

    /**
     * Fusion Core: the node as the server's dispatcher can hold it; {@code node} itself when nothing in its tree needs
     * unwrapping. Used by {@link PaperCommands} for nodes registered through {@link Commands#register}.
     */
    @SuppressWarnings("unchecked")
    public static <N extends CommandNode<CommandSourceStack>> N unwrap(final N node) {
        if (!needsUnwrapping(node, Collections.newSetFromMap(new IdentityHashMap<>()))) {
            return node;
        }
        return (N) unwrapNode(node);
    }

    // Fusion Core: whether the tree holds an API argument type; unknown argument types fail as in Paper's conversion
    private static boolean needsUnwrapping(final CommandNode<?> node, final Set<CommandNode<?>> seen) {
        if (node instanceof RootCommandNode || !seen.add(node)) {
            return false;
        }
        if (UNWRAPPED.containsKey(node)) {
            return true;
        }
        if (node instanceof final ArgumentCommandNode<?, ?> argumentNode) {
            final ArgumentType<?> type = argumentNode.getType();
            if (type instanceof CustomArgumentType<?, ?> || type instanceof VanillaArgumentProviderImpl.NativeWrapperArgumentType<?, ?>) {
                return true;
            }
            if (!(argumentNode instanceof WrappedArgumentCommandNode<?, ?>) && !ArgumentTypeInfos.isClassRecognized(type.getClass())) {
                throw new IllegalArgumentException("Custom unknown argument type was passed, should be wrapped inside an CustomArgumentType.");
            }
        }
        if (node.getRedirect() != null && needsUnwrapping(node.getRedirect(), seen)) {
            return true;
        }
        for (final CommandNode<?> child : node.getChildren()) {
            if (needsUnwrapping(child, seen)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static @NotNull CommandNode<CommandSourceStack> unwrapNode(final CommandNode<CommandSourceStack> maybeWrappedNode) {
        // Fusion Core: a root (usually the server's, as a redirect target) stays as it is, the mirror stands for the server's
        if (maybeWrappedNode instanceof final ApiMirrorRootNode mirror) {
            return (CommandNode) mirror.getDispatcher().getRoot();
        }
        if (maybeWrappedNode instanceof RootCommandNode) {
            return maybeWrappedNode;
        }
        final CommandNode<?> cached = UNWRAPPED.get(maybeWrappedNode);
        if (cached != null) {
            return (CommandNode) cached;
        }
        // Fusion Core: a subtree without API argument types is shared as it is
        if (!needsUnwrapping(maybeWrappedNode, Collections.newSetFromMap(new IdentityHashMap<>()))) {
            return maybeWrappedNode;
        }
        return convertFromPureBrigNode(maybeWrappedNode);
    }

    private static @NotNull CommandNode<CommandSourceStack> convertFromPureBrigNode(final CommandNode<CommandSourceStack> pureNode) {
        final CommandNode<CommandSourceStack> converted;
        if (pureNode instanceof final LiteralCommandNode<CommandSourceStack> node) {
            converted = simpleUnwrap(node);
        } else if (pureNode instanceof final ArgumentCommandNode<CommandSourceStack, ?> pureArgumentNode) {
            final ArgumentType<?> pureArgumentType = pureArgumentNode.getType();
            if (pureArgumentType instanceof final CustomArgumentType<?, ?> customArgumentType) {
                final SuggestionProvider<?> suggestionProvider;
                try {
                    final Method listSuggestions = customArgumentType.getClass().getMethod("listSuggestions", CommandContext.class, SuggestionsBuilder.class);
                    if (listSuggestions.getDeclaringClass() != CustomArgumentType.class) {
                        suggestionProvider = customArgumentType::listSuggestions;
                    } else {
                        suggestionProvider = null;
                    }
                } catch (final NoSuchMethodException ex) {
                    throw new IllegalStateException("Could not determine if the custom argument type " + customArgumentType + " overrides listSuggestions", ex);
                }

                converted = unwrapArgumentWrapper(pureArgumentNode, customArgumentType, customArgumentType.getNativeType(), suggestionProvider);
            } else if (pureArgumentType instanceof final VanillaArgumentProviderImpl.NativeWrapperArgumentType<?, ?> nativeWrapperArgumentType) {
                converted = unwrapArgumentWrapper(pureArgumentNode, nativeWrapperArgumentType, nativeWrapperArgumentType, null); // "null" for suggestion provider so it uses the argument type's suggestion provider
            } else if (pureArgumentNode instanceof WrappedArgumentCommandNode<?, ?> || ArgumentTypeInfos.isClassRecognized(pureArgumentType.getClass())) {
                converted = simpleUnwrap(pureArgumentNode);
            } else {
                throw new IllegalArgumentException("Custom unknown argument type was passed, should be wrapped inside an CustomArgumentType.");
            }
        } else {
            throw new IllegalArgumentException("Unknown command node passed. Don't know how to unwrap this.");
        }

        // Store unwrapped node before unwrapping children to avoid infinite recursion in cyclic redirects.
        UNWRAPPED.put(pureNode, converted);

        for (final CommandNode<CommandSourceStack> child : pureNode.getChildren()) {
            converted.addChild(unwrapNode(child));
        }

        return converted;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void addChild(CommandNode<CommandSourceStack> node) {
        CommandNode convertedNode = unwrapNode(node);
        this.addUnwrapped(convertedNode);
    }

    // Fusion Core: where an unwrapped node goes; PaperCommands also puts it into Bukkit's command map
    protected void addUnwrapped(CommandNode<net.minecraft.commands.CommandSourceStack> node) {
        this.getDispatcher().getRoot().addChild(node);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Collection<CommandNode<CommandSourceStack>> getChildren() {
        return (Collection) this.getDispatcher().getRoot().getChildren();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public CommandNode<CommandSourceStack> getChild(String name) {
        return (CommandNode) this.getDispatcher().getRoot().getChild(name);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static CommandNode<CommandSourceStack> unwrapArgumentWrapper(final ArgumentCommandNode pureNode, final ArgumentType pureArgumentType, final ArgumentType possiblyWrappedNativeArgumentType, @Nullable SuggestionProvider argumentTypeSuggestionProvider) {
        validatePrimitiveType(possiblyWrappedNativeArgumentType);
        final CommandNode redirectNode = pureNode.getRedirect() == null ? null : unwrapNode(pureNode.getRedirect());
        // If there is already a custom suggestion provider, ignore the suggestion provider from the argument type
        final SuggestionProvider suggestionProvider = pureNode.getCustomSuggestions() != null ? pureNode.getCustomSuggestions() : argumentTypeSuggestionProvider;

        final ArgumentType nativeArgumentType = possiblyWrappedNativeArgumentType instanceof final VanillaArgumentProviderImpl.NativeWrapperArgumentType<?, ?> nativeWrapperArgumentType ? nativeWrapperArgumentType.nativeNmsArgumentType() : possiblyWrappedNativeArgumentType;
        return new WrappedArgumentCommandNode<>(pureNode.getName(), pureArgumentType, nativeArgumentType, pureNode.getCommand(), pureNode.getRequirement(), redirectNode, pureNode.getRedirectModifier(), pureNode.isFork(), suggestionProvider);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static CommandNode<CommandSourceStack> simpleUnwrap(final CommandNode<CommandSourceStack> node) {
        // Fusion Core: a WrappedArgumentCommandNode's builder would drop the API type, so it is rebuilt as itself
        if (node instanceof final WrappedArgumentCommandNode wrapped) {
            return wrapped.copy(node.getRedirect() == null ? null : unwrapNode(node.getRedirect()));
        }
        return node.createBuilder()
            .redirect(node.getRedirect() == null ? null : unwrapNode(node.getRedirect()))
            .build();
    }
}
