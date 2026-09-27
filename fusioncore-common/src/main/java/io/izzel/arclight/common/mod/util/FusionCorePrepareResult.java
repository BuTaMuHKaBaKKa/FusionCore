package io.izzel.arclight.common.mod.util;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import io.izzel.arclight.common.bridge.core.world.inventory.AbstractContainerMenuBridge;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareInventoryResultEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.view.AnvilView;

/** Fusion Core: Paper's {@code callPrepareResultEvent} (patch 0382), called after a station computes its result. */
public final class FusionCorePrepareResult {

    private FusionCorePrepareResult() {
    }

    public static void call(AbstractContainerMenu menu, int resultSlot) {
        InventoryView view = ((AbstractContainerMenuBridge) menu).bridge$getBukkitView();
        if (view == null) {
            return;
        }
        var top = view.getTopInventory();
        // PrepareResultEvent has no HandlerList of its own and uses PrepareInventoryResultEvent's, as in Paper
        var handlers = top instanceof AnvilInventory && view instanceof AnvilView ? PrepareAnvilEvent.getHandlerList()
            : top instanceof GrindstoneInventory ? PrepareGrindstoneEvent.getHandlerList()
            : top instanceof SmithingInventory ? PrepareSmithingEvent.getHandlerList()
            : PrepareInventoryResultEvent.getHandlerList();
        if (handlers.getRegisteredListeners().length == 0) {
            return;
        }
        var origItem = top.getItem(resultSlot);
        CraftItemStack result = origItem != null ? CraftItemStack.asCraftCopy(origItem) : null;
        PrepareInventoryResultEvent event;
        if (top instanceof AnvilInventory && view instanceof AnvilView anvilView) {
            event = new PrepareAnvilEvent(anvilView, result);
        } else if (top instanceof GrindstoneInventory) {
            event = new PrepareGrindstoneEvent(view, result);
        } else if (top instanceof SmithingInventory) {
            event = new PrepareSmithingEvent(view, result);
        } else {
            event = new PrepareResultEvent(view, result);
        }
        Bukkit.getPluginManager().callEvent(event);
        event.getInventory().setItem(resultSlot, event.getResult());
        menu.broadcastChanges();
    }
}
