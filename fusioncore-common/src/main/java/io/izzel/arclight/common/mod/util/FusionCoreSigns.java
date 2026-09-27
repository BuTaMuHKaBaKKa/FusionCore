package io.izzel.arclight.common.mod.util;

import net.kyori.adventure.text.Component;
import org.bukkit.block.sign.SignSide;

import java.util.AbstractList;
import java.util.List;

/** Fusion Core: Paper's component sign lines, backed by Spigot's legacy lines. */
public final class FusionCoreSigns {

    private FusionCoreSigns() {
    }

    public static Component line(SignSide side, int index) {
        return FusionCoreAdventure.fromLegacy(side.getLine(index));
    }

    public static void line(SignSide side, int index, Component line) {
        side.setLine(index, FusionCoreAdventure.toLegacyRgb(line == null ? Component.empty() : line));
    }

    public static List<Component> lines(SignSide side) {
        return new AbstractList<>() {
            @Override
            public Component get(int index) {
                return line(side, index);
            }

            @Override
            public Component set(int index, Component element) {
                Component old = line(side, index);
                line(side, index, element);
                return old;
            }

            @Override
            public int size() {
                return side.getLines().length;
            }
        };
    }
}
