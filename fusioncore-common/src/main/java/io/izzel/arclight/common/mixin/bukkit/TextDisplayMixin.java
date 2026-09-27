package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.TextDisplay;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code TextDisplay#text()}; {@code CraftTextDisplayMixin} overrides these defaults. */
@Mixin(value = TextDisplay.class, remap = false)
public interface TextDisplayMixin {

    default Component text() {
        String legacy = ((TextDisplay) this).getText();
        return legacy == null ? null : FusionCoreAdventure.fromLegacy(legacy);
    }

    default void text(Component text) {
        ((TextDisplay) this).setText(text == null ? null : FusionCoreAdventure.toLegacyRgb(text));
    }
}
