// Fusion Core: Paper 1.21.1 server class (patch 0965, Brigadier command API). Paper adds an overload of
// MessageArgument#resolveChatMessage taking the message; here MessageArgumentMixin lets the vanilla method read the message
// out of this resolver, and the chat message is handed over as FusionCoreSignedMessage (Paper's adventureView()).
package io.papermc.paper.command.brigadier.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.izzel.arclight.common.mod.util.FusionCoreSignedMessage;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.chat.SignedMessage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import org.jetbrains.annotations.NotNull;

public record SignedMessageResolverImpl(MessageArgument.Message message) implements SignedMessageResolver {

    @Override
    public @NotNull String content() {
        return this.message.text();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public @NotNull CompletableFuture<SignedMessage> resolveSignedMessage(final @NotNull String argumentName, final @NotNull CommandContext erased) throws CommandSyntaxException {
        final CompletableFuture<SignedMessage> future = new CompletableFuture<>();

        final CommandContext<CommandSourceStack> context = (CommandContext<CommandSourceStack>) erased;
        MessageArgument.resolveChatMessage(context, argumentName, (message) -> {
            future.complete(new FusionCoreSignedMessage(message, context.getSource().registryAccess()));
        });
        return future;
    }
}
