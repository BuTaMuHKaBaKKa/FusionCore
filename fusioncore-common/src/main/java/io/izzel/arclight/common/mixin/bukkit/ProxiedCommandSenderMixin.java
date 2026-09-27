package io.izzel.arclight.common.mixin.bukkit;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.command.ProxiedCommandSender;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@link ProxiedCommandSender} is a single forwarding audience to its caller (API patch 0006
 * "Adventure"); messages sent to it reach whoever ran the command.
 */
@Mixin(value = ProxiedCommandSender.class, remap = false)
public interface ProxiedCommandSenderMixin extends ForwardingAudience.Single {

    @Override
    default Audience audience() {
        return (Audience) (Object) ((ProxiedCommandSender) this).getCaller();
    }

    // both CommandSender (Fusion Core's CommandSenderMixin) and ForwardingAudience.Single have a default: pick Paper's
    @Override
    @SuppressWarnings("deprecation")
    default void sendMessage(Identity source, Component message, MessageType type) {
        this.audience().sendMessage(source, message, type);
    }
}
