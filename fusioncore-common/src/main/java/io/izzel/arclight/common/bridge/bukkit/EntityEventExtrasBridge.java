package io.izzel.arclight.common.bridge.bukkit;

import org.bukkit.PortalType;
import org.bukkit.block.data.BlockData;

/** Fusion Core: setters for what Paper's constructors of entity events carry, for the factories in {@code FusionCoreEvents}. */
public interface EntityEventExtrasBridge {

    /** {@code EntityPortalEnterEvent}, {@code EntityPortalEvent} (Paper patch 0781). */
    interface Portal {
        void bridge$setPortalType(PortalType type);
    }

    /** {@code EntityRegainHealthEvent} (Paper patch 0091). */
    interface RegainHealth {
        void bridge$setFastRegen(boolean fastRegen);
    }

    /** {@code EntityDismountEvent}, {@code VehicleExitEvent} (Paper patch 0155). */
    interface Cancellability {
        void bridge$setCancellable(boolean cancellable);
    }

    /** {@code EntityChangeBlockEvent} and {@code EntityBreakDoorEvent} (Paper patch 0683). */
    interface ChangeBlock {
        void bridge$setBlockData(BlockData to);
    }
}
