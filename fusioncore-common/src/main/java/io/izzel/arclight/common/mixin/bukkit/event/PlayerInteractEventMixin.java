package io.izzel.arclight.common.mixin.bukkit.event;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code PlayerInteractEvent#getInteractionPoint} (API patch 0234): the exact clicked point. */
@Mixin(value = PlayerInteractEvent.class, remap = false)
public abstract class PlayerInteractEventMixin {

    @Shadow protected Block blockClicked;
    @Shadow private Vector clickedPosistion;

    public Location getInteractionPoint() {
        if (this.blockClicked == null || this.clickedPosistion == null) {
            return null;
        }
        return this.blockClicked.getLocation().add(this.clickedPosistion);
    }
}
