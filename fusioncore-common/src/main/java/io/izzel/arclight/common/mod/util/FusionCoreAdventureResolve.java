package io.izzel.arclight.common.mod.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ComponentUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.entity.Entity;

import java.io.IOException;

/**
 * Fusion Core: Paper's {@code resolveWithContext}. {@code bypassPermissions} is accepted but the sender's permissions apply.
 */
public final class FusionCoreAdventureResolve {

    private FusionCoreAdventureResolve() {
    }

    public static Component resolveWithContext(Component component, CommandSender context, Entity scoreboardSubject, boolean bypassPermissions) throws IOException {
        CommandSourceStack source = context != null ? VanillaCommandWrapper.getListener(context)
            : ArclightServer.getMinecraftServer().createCommandSourceStack();
        try {
            return FusionCoreAdventure.asAdventure(ComponentUtils.updateForEntity(source, FusionCoreAdventure.asVanilla(component),
                scoreboardSubject == null ? null : ((CraftEntity) scoreboardSubject).getHandle(), 0));
        } catch (CommandSyntaxException e) {
            throw new IOException(e);
        }
    }
}
