package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.Display;
import org.bukkit.craftbukkit.v.entity.CraftTextDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Fusion Core: Paper's {@code TextDisplay#text()} on the real entity, converted to and from the
 * vanilla component directly so nothing is lost (see {@link TextDisplayMixin}).
 */
@Mixin(value = CraftTextDisplay.class, remap = false)
public abstract class CraftTextDisplayMixin {

    @Shadow public abstract Display.TextDisplay getHandle();

    public Component text() {
        Display.TextDisplay handle = this.getHandle();
        return FusionCoreAdventure.asAdventure(handle.getText(), handle.registryAccess());
    }

    public void text(Component text) {
        Display.TextDisplay handle = this.getHandle();
        handle.setText(text == null ? net.minecraft.network.chat.Component.empty() : FusionCoreAdventure.asVanilla(text, handle.registryAccess()));
    }
}
