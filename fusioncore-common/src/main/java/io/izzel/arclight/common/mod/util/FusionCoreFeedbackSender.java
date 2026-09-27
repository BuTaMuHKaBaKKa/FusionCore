package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.server.ArclightServer;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.command.ServerCommandSender;

import java.util.function.Consumer;

/** Fusion Core: Paper's {@code FeedbackForwardingSender} (patch 0658). Has its own permissions, not the console's. */
public final class FusionCoreFeedbackSender extends ServerCommandSender implements net.kyori.adventure.audience.Audience {

    private final Consumer<? super Component> feedback;

    public FusionCoreFeedbackSender(Consumer<? super Component> feedback) {
        this.feedback = feedback;
    }

    @Override
    public void sendMessage(String message) {
        this.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            this.sendMessage(message);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendMessage(Identity identity, Component message, MessageType type) {
        this.feedback.accept(message);
    }

    @Override
    public String getName() {
        return "FeedbackForwardingSender";
    }

    public Component name() {
        return Component.text(this.getName());
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
        throw new UnsupportedOperationException("Cannot change operator status of " + this.getClass().getName());
    }

    /** The vanilla source vanilla commands run as, at the overworld spawn with permission level 4. */
    public CommandSourceStack asVanilla() {
        MinecraftServer server = ArclightServer.getMinecraftServer();
        ServerLevel overworld = server.overworld();
        return new CommandSourceStack(new Source(this), overworld == null ? Vec3.ZERO : Vec3.atLowerCornerOf(overworld.getSharedSpawnPos()),
            Vec2.ZERO, overworld, 4, this.getName(), net.minecraft.network.chat.Component.literal(this.getName()), server, null);
    }

    private record Source(FusionCoreFeedbackSender sender) implements CommandSource, io.izzel.arclight.common.bridge.core.command.CommandSourceBridge {

        @Override
        public void sendSystemMessage(net.minecraft.network.chat.Component message) {
            this.sender.sendMessage(Identity.nil(), FusionCoreAdventure.asAdventure(message), MessageType.SYSTEM);
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }

        @Override
        public CommandSender bridge$getBukkitSender(CommandSourceStack wrapper) {
            return this.sender;
        }
    }
}
