// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.event.block;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Called when a block tries to dispense an item, but its inventory is empty.
 */
public class BlockFailedDispenseEvent extends BlockEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean shouldPlayEffect = true;

    @ApiStatus.Internal
    public BlockFailedDispenseEvent(final Block theBlock) {
        super(theBlock);
    }

    /**
     * @return if the effect should be played
     */
    public boolean shouldPlayEffect() {
        return this.shouldPlayEffect;
    }

    /**
     * Sets if the effect for empty dispensers should be played
     *
     * @param playEffect if the effect should be played
     */
    public void shouldPlayEffect(final boolean playEffect) {
        this.shouldPlayEffect = playEffect;
    }

    /**
     * @return {@link #shouldPlayEffect()}
     */
    // Fusion Core: overrides Event#callEvent, which EventMixin adds at runtime (javac does not see it, hence no @Override/super)
    public boolean callEvent() {
        org.bukkit.Bukkit.getPluginManager().callEvent(this);
        return this.shouldPlayEffect();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
