package io.izzel.arclight.common.mixin.core.commands.arguments;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.context.CommandContext;
import io.izzel.arclight.common.mod.util.FusionCorePaperPlayerEvents;
import io.papermc.paper.command.brigadier.argument.SignedMessageResolverImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.network.FilteredText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Fusion Core: Paper's AsyncChatCommandDecorateEvent for chat arguments of /say, /me, /msg, and the resolver of
 * {@code ArgumentTypes#signedMessage()}.
 */
@Mixin(MessageArgument.class)
public abstract class MessageArgumentMixin {

    @Shadow
    private static CompletableFuture<FilteredText> filterPlainText(CommandSourceStack source, PlayerChatMessage message) {
        throw new AbstractMethodError();
    }

    /**
     * @author Fusion Core
     * @reason Paper's asynchronous chat decoration
     */
    @Overwrite
    private static void resolveSignedMessage(Consumer<PlayerChatMessage> callback, CommandSourceStack source, PlayerChatMessage message) {
        CompletableFuture<FilteredText> filtered = filterPlainText(source, message);
        Component vanilla = source.getServer().getChatDecorator().decorate(source.getPlayer(), message.decoratedContent());
        CompletableFuture<Component> decorated = FusionCorePaperPlayerEvents.decorateChat(source.getPlayer(), true, vanilla);
        source.getChatMessageChainer().append(CompletableFuture.allOf(filtered, decorated),
            ignored -> callback.accept(message.withUnsignedContent(decorated.join()).filter(filtered.join().mask())));
    }

    /**
     * @author Fusion Core
     * @reason Paper's asynchronous chat decoration
     */
    @Overwrite
    private static void resolveDisguisedMessage(Consumer<PlayerChatMessage> callback, CommandSourceStack source, PlayerChatMessage message) {
        Component vanilla = source.getServer().getChatDecorator().decorate(source.getPlayer(), message.decoratedContent());
        CompletableFuture<Component> decorated = FusionCorePaperPlayerEvents.decorateChat(source.getPlayer(), true, vanilla);
        if (decorated.isDone()) {
            callback.accept(message.withUnsignedContent(decorated.join()));
        } else {
            source.getChatMessageChainer().append(decorated, result -> callback.accept(message.withUnsignedContent(result)));
        }
    }

    @WrapOperation(method = "resolveChatMessage", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/context/CommandContext;getArgument(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", remap = false))
    private static Object fusioncore$paperSignedMessage(CommandContext<?> context, String name, Class<?> type, Operation<Object> original) {
        if (context.getArgument(name, Object.class) instanceof SignedMessageResolverImpl resolver) {
            return resolver.message();
        }
        return original.call(context, name, type);
    }
}
