package io.izzel.arclight.common.bridge.core.world.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryView;

public interface AbstractContainerMenuBridge {

    InventoryView bridge$getBukkitView();

    void bridge$transferTo(AbstractContainerMenu other, CraftHumanEntity player);

    Component bridge$getTitle();

    void bridge$setTitle(Component title);

    /** Fusion Core: Paper's {@code InventoryOpenEvent#titleOverride} for this menu, null when none. */
    Component fusioncore$getTitleOverride();

    void fusioncore$setTitleOverride(Component title);

    boolean bridge$isCheckReachable();

    /** Fusion Core: Paper's forced menus (Paper patch 0408) skip the reach check. */
    void fusioncore$setCheckReachable(boolean checkReachable);
}
