package io.izzel.arclight.common.mixin.bukkit.inventory;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.craftbukkit.v.inventory.trim.CraftTrimPattern;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code TrimPattern#description()} (Paper patch 0006 "Adventure"): the vanilla description. */
@Mixin(value = TrimPattern.class, remap = false)
public interface TrimPatternMixin {

    default Component description() {
        return FusionCoreAdventure.asAdventure(((CraftTrimPattern) (Object) this).getHandle().description());
    }
}
