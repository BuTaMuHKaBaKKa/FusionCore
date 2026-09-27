package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code HumanEntity} API. */
@Mixin(value = org.bukkit.entity.HumanEntity.class, remap = false)
public interface HumanEntityMixin {

    /** Paper: the respawn point (bed or anchor) of a player, or null. */
    default org.bukkit.Location getPotentialBedLocation() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.potentialBedLocation((org.bukkit.entity.HumanEntity) this);
    }

    /** Paper: closes the open inventory with the given close reason (Paper patch 0208). */
    default void closeInventory(org.bukkit.event.inventory.InventoryCloseEvent$Reason reason) {
        io.izzel.arclight.common.mod.util.FusionCoreEvents.closeWith(reason, ((org.bukkit.entity.HumanEntity) this)::closeInventory);
    }

    // Fusion Core: Paper's smaller HumanEntity API, bodies in FusionCorePlayers

    default org.bukkit.entity.FishHook getFishHook() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.fishHook((org.bukkit.entity.HumanEntity) this);
    }

    default boolean isDeeplySleeping() {
        return ((org.bukkit.craftbukkit.v.entity.CraftHumanEntity) (Object) this).getHandle().isSleepingLongEnough();
    }

    default org.bukkit.entity.Entity releaseLeftShoulderEntity() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.releaseShoulder((org.bukkit.entity.HumanEntity) this, true);
    }

    default org.bukkit.entity.Entity releaseRightShoulderEntity() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.releaseShoulder((org.bukkit.entity.HumanEntity) this, false);
    }

    default void setHurtDirection(float hurtDirection) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.setHurtDirection((org.bukkit.entity.HumanEntity) this, hurtDirection);
    }

    default void openSign(org.bukkit.block.Sign sign) {
        this.openSign(sign, org.bukkit.block.sign.Side.FRONT);
    }

    /** Spigot has it on Player only; Paper moved it up. Other human entities cannot edit signs. */
    default void openSign(org.bukkit.block.Sign sign, org.bukkit.block.sign.Side side) {
        if (this instanceof org.bukkit.entity.Player player) {
            player.openSign(sign, side);
        }
    }

    default org.bukkit.inventory.InventoryView openAnvil(org.bukkit.Location location, boolean force) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.openBlockMenu((org.bukkit.entity.HumanEntity) this, location, force, org.bukkit.Material.ANVIL);
    }

    default org.bukkit.inventory.InventoryView openCartographyTable(org.bukkit.Location location, boolean force) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.openBlockMenu((org.bukkit.entity.HumanEntity) this, location, force, org.bukkit.Material.CARTOGRAPHY_TABLE);
    }

    default org.bukkit.inventory.InventoryView openGrindstone(org.bukkit.Location location, boolean force) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.openBlockMenu((org.bukkit.entity.HumanEntity) this, location, force, org.bukkit.Material.GRINDSTONE);
    }

    default org.bukkit.inventory.InventoryView openLoom(org.bukkit.Location location, boolean force) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.openBlockMenu((org.bukkit.entity.HumanEntity) this, location, force, org.bukkit.Material.LOOM);
    }

    default org.bukkit.inventory.InventoryView openSmithingTable(org.bukkit.Location location, boolean force) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.openBlockMenu((org.bukkit.entity.HumanEntity) this, location, force, org.bukkit.Material.SMITHING_TABLE);
    }

    default org.bukkit.inventory.InventoryView openStonecutter(org.bukkit.Location location, boolean force) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.openBlockMenu((org.bukkit.entity.HumanEntity) this, location, force, org.bukkit.Material.STONECUTTER);
    }
}
