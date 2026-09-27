package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;

/** Fusion Core: Paper's PlayerInventorySlotChangeEvent (patch 0750). */
public final class FusionCoreInventoryListener implements ContainerListener {

    private final ServerPlayer player;
    private final ContainerListener vanilla;

    public FusionCoreInventoryListener(ServerPlayer player, ContainerListener vanilla) {
        this.player = player;
        this.vanilla = vanilla;
    }

    @Override
    public void slotChanged(AbstractContainerMenu menu, int slotId, ItemStack stack) {
        this.vanilla.slotChanged(menu, slotId, stack);
    }

    /** Paper's {@code slotChanged(handler, slotId, oldStack, stack)}. */
    public void slotChanged(AbstractContainerMenu menu, int slotId, ItemStack oldStack, ItemStack stack) {
        if (PlayerInventorySlotChangeEvent.getHandlerList().getRegisteredListeners().length == 0) {
            this.vanilla.slotChanged(menu, slotId, stack);
            return;
        }
        Slot slot = menu.getSlot(slotId);
        if (slot instanceof ResultSlot || slot.container != this.player.getInventory()) {
            this.vanilla.slotChanged(menu, slotId, stack); // does nothing for these slots, kept for mods that hook it
            return;
        }
        PlayerInventorySlotChangeEvent event = new PlayerInventorySlotChangeEvent(((ServerPlayerBridge) this.player).bridge$getBukkitEntity(),
            slotId, CraftItemStack.asBukkitCopy(oldStack), CraftItemStack.asBukkitCopy(stack));
        Bukkit.getPluginManager().callEvent(event);
        if (event.shouldTriggerAdvancements()) {
            this.vanilla.slotChanged(menu, slotId, stack);
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu menu, int property, int value) {
        this.vanilla.dataChanged(menu, property, value);
    }
}
