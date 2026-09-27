package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.ServerLinks} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.ServerLinks.class, remap = false)
public interface ServerLinksMixin {

    default org.bukkit.ServerLinks.ServerLink addLink(net.kyori.adventure.text.Component displayName, java.net.URI url) {
        return ((org.bukkit.ServerLinks) this).addLink(io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(displayName), url);
    }
}
