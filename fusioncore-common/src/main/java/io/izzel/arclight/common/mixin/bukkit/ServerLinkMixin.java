package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.ServerLinks.ServerLink} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.ServerLinks.ServerLink.class, remap = false)
public interface ServerLinkMixin {

    default net.kyori.adventure.text.Component displayName() {
        return io.izzel.arclight.common.mod.util.FusionCoreAdventure.fromLegacy(((org.bukkit.ServerLinks.ServerLink) this).getDisplayName());
    }
}
