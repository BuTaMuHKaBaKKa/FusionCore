package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.map.MapCursorCollection} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.map.MapCursorCollection.class, remap = false)
public abstract class MapCursorCollectionMixin {

    @Shadow public abstract org.bukkit.map.MapCursor addCursor(int x, int y, byte direction, byte type, boolean visible, String caption);

    public org.bukkit.map.MapCursor addCursor(int x, int y, byte direction, byte type, boolean visible, net.kyori.adventure.text.Component caption) {
        return this.addCursor(x, y, direction, type, visible, caption == null ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(caption));
    }
}
