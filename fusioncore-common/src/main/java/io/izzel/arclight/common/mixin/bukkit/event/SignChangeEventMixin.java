package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.event.block.SignChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.AbstractList;
import java.util.List;

/** Fusion Core: Paper's Adventure view of sign lines, backed by Spigot's legacy lines. */
@Mixin(value = SignChangeEvent.class, remap = false)
public abstract class SignChangeEventMixin {

    @Shadow public abstract String[] getLines();

    @Shadow public abstract String getLine(int index);

    @Shadow public abstract void setLine(int index, String line);

    public List<Component> lines() {
        return new AbstractList<>() {
            @Override
            public Component get(int index) {
                return line(index);
            }

            @Override
            public Component set(int index, Component element) {
                Component previous = line(index);
                line(index, element);
                return previous;
            }

            @Override
            public int size() {
                return getLines().length;
            }
        };
    }

    public Component line(int index) {
        String legacy = this.getLine(index);
        return legacy == null ? null : FusionCoreAdventure.fromLegacy(legacy);
    }

    public void line(int index, Component line) {
        this.setLine(index, line == null ? null : FusionCoreAdventure.toLegacyRgb(line));
    }
}
