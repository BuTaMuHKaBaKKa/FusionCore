package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Spigot's {@code hopper-amount}: how many items a hopper moves out of one slot in one transfer. The hopper takes up
 * to that many items, one {@code InventoryMoveItemEvent} carries all of them, and what did not fit goes back to the
 * slot it came from.
 */
public final class FusionCoreHoppers {

    private FusionCoreHoppers() {
    }

    public static int amount(Level level) {
        return level == null ? 1 : Math.max(1, ((WorldBridge) level).bridge$spigotConfig().hopperAmount);
    }

    /** The amount for a hopper block or a hopper minecart. */
    public static int amount(Object hopper) {
        if (hopper instanceof BlockEntity blockEntity) {
            return amount(blockEntity.getLevel());
        } else if (hopper instanceof Entity entity) {
            return amount(entity.level());
        }
        return 1;
    }

    /**
     * Vanilla puts the whole count back into the slot when the moved stack did not fit completely, which is right
     * only for one item. Takes off what did move (Spigot) and writes the slot back when it was emptied.
     */
    public static void restore(Container container, int slot, ItemStack stack, int count, ItemStack leftover, int amount) {
        int moved = Math.min(amount, count);
        stack.setCount(count - (moved - Math.min(moved, leftover.getCount())));
        if (count > 1 && count <= amount) {
            container.setItem(slot, stack);
        }
    }
}
