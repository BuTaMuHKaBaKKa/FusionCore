package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.Nameable;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code customName()} on {@link Nameable}. Block states go through the legacy name. */
@Mixin(value = Nameable.class, remap = false)
public interface NameableMixin {

    default Component customName() {
        String name = ((Nameable) this).getCustomName();
        return name == null ? null : FusionCoreAdventure.fromLegacy(name);
    }

    default void customName(Component customName) {
        ((Nameable) this).setCustomName(customName == null ? null : FusionCoreAdventure.toLegacyRgb(customName));
    }
}
