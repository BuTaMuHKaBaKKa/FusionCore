package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.map.MapCursor} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.map.MapCursor.class, remap = false)
public abstract class MapCursorMixin {

    @Shadow public abstract String getCaption();

    @Shadow public abstract void setCaption(String caption);

    public net.kyori.adventure.text.Component caption() {
        String caption = this.getCaption();
        return caption == null ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.fromLegacy(caption);
    }

    public void caption(net.kyori.adventure.text.Component caption) {
        this.setCaption(caption == null ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(caption));
    }
}
