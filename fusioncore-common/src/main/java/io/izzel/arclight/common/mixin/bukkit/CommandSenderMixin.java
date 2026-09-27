package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: every {@link CommandSender} is an Adventure {@link Audience}, as in Paper. Non-player senders get
 * legacy text; players are handled in {@code CraftPlayerAudienceMixin}.
 */
@Mixin(value = CommandSender.class, remap = false)
public interface CommandSenderMixin extends Audience {

    @Override
    @SuppressWarnings("deprecation")
    default void sendMessage(final Identity source, final Component message, final MessageType type) {
        ((CommandSender) this).sendMessage(FusionCoreAdventure.toLegacy((CommandSender) this, message));
    }

    // Fusion Core: the rest of Paper's CommandSender text API (Paper patch 0006 "Adventure")

    /** Paper: the sender's name as a component; entities override it with their vanilla name. */
    default Component name() {
        return Component.text(((CommandSender) this).getName());
    }

    default void sendRichMessage(final String message) {
        this.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(message));
    }

    default void sendRichMessage(final String message, final net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... resolvers) {
        this.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(message, resolvers));
    }

    default void sendPlainMessage(final String message) {
        this.sendMessage(Component.text(message));
    }

    // Fusion Core: Paper puts Spigot's BungeeCord-component messages on CommandSender itself
    default void sendMessage(net.md_5.bungee.api.chat.BaseComponent component) {
        ((CommandSender) this).spigot().sendMessage(component);
    }

    default void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components) {
        ((CommandSender) this).spigot().sendMessage(components);
    }

    default void sendMessage(java.util.UUID sender, net.md_5.bungee.api.chat.BaseComponent component) {
        ((CommandSender) this).spigot().sendMessage(sender, component);
    }

    default void sendMessage(java.util.UUID sender, net.md_5.bungee.api.chat.BaseComponent... components) {
        ((CommandSender) this).spigot().sendMessage(sender, components);
    }

    /** Paper: display name and permissions for Adventure (Paper patch 0010). */
    default net.kyori.adventure.pointer.Pointers pointers() {
        org.bukkit.command.CommandSender self = (org.bukkit.command.CommandSender) this;
        return net.kyori.adventure.pointer.Pointers.builder()
            .withDynamic(net.kyori.adventure.identity.Identity.DISPLAY_NAME, this::name)
            .withStatic(net.kyori.adventure.permission.PermissionChecker.POINTER, permission -> io.izzel.arclight.common.mod.util.FusionCoreAudiences.permissionValue(self, permission))
            .build();
    }
}
