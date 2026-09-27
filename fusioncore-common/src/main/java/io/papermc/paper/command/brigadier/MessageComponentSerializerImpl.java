// Fusion Core: Paper 1.21.1 server class (patch 0965, Brigadier command API); FusionCoreAdventure in place of PaperAdventure.
package io.papermc.paper.command.brigadier;

import com.mojang.brigadier.Message;
import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.ComponentUtils;
import org.jetbrains.annotations.NotNull;

public final class MessageComponentSerializerImpl implements MessageComponentSerializer {

    static final MessageComponentSerializerImpl INSTANCE = new MessageComponentSerializerImpl();

    @Override
    public @NotNull Component deserialize(@NotNull Message input) {
        return FusionCoreAdventure.asAdventure(ComponentUtils.fromMessage(input));
    }

    @Override
    public @NotNull Message serialize(@NotNull Component component) {
        return FusionCoreAdventure.asVanilla(component);
    }
}
