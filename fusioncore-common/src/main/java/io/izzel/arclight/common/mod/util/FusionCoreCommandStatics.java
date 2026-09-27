package io.izzel.arclight.common.mod.util;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Fusion Core: Paper's static methods on {@link Command} that Spigot lacks (Paper patch 0006 "Adventure"),
 * reached through {@code PaperCallTransformer} like {@link FusionCoreBukkit}.
 */
public final class FusionCoreCommandStatics {

    private FusionCoreCommandStatics() {
    }

    /** Paper: {@code broadcastCommandMessage(source, message, true)}. */
    public static void broadcastCommandMessage(CommandSender source, Component message) {
        broadcastCommandMessage(source, message, true);
    }

    /** Goes through Spigot's string variant with legacy text. */
    public static void broadcastCommandMessage(CommandSender source, Component message, boolean sendToSource) {
        Command.broadcastCommandMessage(source, FusionCoreAdventure.toLegacyRgb(message), sendToSource);
    }
}
