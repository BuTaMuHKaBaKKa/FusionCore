package io.izzel.arclight.common.mod.util;

import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/** Fusion Core: Adventure view of a {@link PlayerChatMessage} for {@code AbstractChatEvent#signedMessage()}. */
public final class FusionCoreSignedMessage implements SignedMessage {

    private final PlayerChatMessage message;
    private final HolderLookup.Provider registries;

    public FusionCoreSignedMessage(PlayerChatMessage message, HolderLookup.Provider registries) {
        this.message = message;
        this.registries = registries;
    }

    public PlayerChatMessage playerChatMessage() {
        return this.message;
    }

    @Override
    public @NotNull Instant timestamp() {
        return this.message.timeStamp();
    }

    @Override
    public long salt() {
        return this.message.salt();
    }

    @Override
    public Signature signature() {
        return this.message.signature() == null ? null : SignedMessage.signature(this.message.signature().bytes());
    }

    @Override
    public Component unsignedContent() {
        return this.message.unsignedContent() == null ? null : FusionCoreAdventure.asAdventure(this.message.unsignedContent(), this.registries);
    }

    @Override
    public @NotNull String message() {
        return this.message.signedContent();
    }

    @Override
    public @NotNull Identity identity() {
        return Identity.identity(this.message.sender());
    }
}
