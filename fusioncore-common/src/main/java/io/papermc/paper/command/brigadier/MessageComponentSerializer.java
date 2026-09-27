// Fusion Core: Paper API 1.21.1 as published in paper-api sources; the jspecify annotations are swapped for JetBrains ones and message() returns
// the core's serializer directly.
package io.papermc.paper.command.brigadier;

import com.mojang.brigadier.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.jetbrains.annotations.ApiStatus;

/**
 * A component serializer for converting between {@link Message} and {@link Component}.
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface MessageComponentSerializer extends ComponentSerializer<Component, Component, Message> {

    /**
     * A component serializer for converting between {@link Message} and {@link Component}.
     *
     * @return serializer instance
     */
    static MessageComponentSerializer message() {
        return MessageComponentSerializerImpl.INSTANCE; // Fusion Core: the core's serializer directly, Paper finds it with ServiceLoader
    }
}
