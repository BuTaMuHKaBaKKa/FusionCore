package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreSigns;
import net.kyori.adventure.text.Component;
import org.bukkit.block.sign.SignSide;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/** Fusion Core: Paper's component lines on {@link SignSide} (Paper patch 0006 "Adventure"), see {@link FusionCoreSigns}. */
@Mixin(value = SignSide.class, remap = false)
public interface SignSideMixin {

    default List<Component> lines() {
        return FusionCoreSigns.lines((SignSide) this);
    }

    default Component line(int index) {
        return FusionCoreSigns.line((SignSide) this, index);
    }

    default void line(int index, Component line) {
        FusionCoreSigns.line((SignSide) this, index, line);
    }
}
