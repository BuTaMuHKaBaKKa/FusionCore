package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreScoreboards;
import org.bukkit.scoreboard.DisplaySlot;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code DisplaySlot#getId}. Constant names stay Spigot's. */
@Mixin(value = DisplaySlot.class, remap = false)
public abstract class DisplaySlotMixin {

    public String getId() {
        return FusionCoreScoreboards.slotId((DisplaySlot) (Object) this);
    }

    public String toString() {
        return this.getId();
    }
}
