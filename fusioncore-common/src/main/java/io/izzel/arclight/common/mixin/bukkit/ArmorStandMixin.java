package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.world.entity.ArmorStandPaperBridge;
import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import io.papermc.paper.math.Rotations;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Fusion Core: Paper's armor stand API (patches 0118, 0217, 0219). */
@Mixin(value = org.bukkit.entity.ArmorStand.class, remap = false)
public interface ArmorStandMixin {

    default boolean canMove() {
        return ((ArmorStandPaperBridge) FusionCoreEntities.handle(this)).fusioncore$canMove();
    }

    default void setCanMove(boolean move) {
        ((ArmorStandPaperBridge) FusionCoreEntities.handle(this)).fusioncore$setCanMove(move);
    }

    default boolean canTick() {
        return ((ArmorStandPaperBridge) FusionCoreEntities.handle(this)).fusioncore$canTick();
    }

    default void setCanTick(boolean tick) {
        ((ArmorStandPaperBridge) FusionCoreEntities.handle(this)).fusioncore$setCanTick(tick);
    }

    default ItemStack getItem(EquipmentSlot slot) {
        com.google.common.base.Preconditions.checkArgument(slot != null, "slot");
        com.google.common.base.Preconditions.checkArgument(slot != EquipmentSlot.BODY, "Cannot get body item");
        return CraftItemStack.asCraftMirror(FusionCoreEntities.living(this).getItemBySlot(CraftEquipmentSlot.getNMS(slot)));
    }

    default void setItem(EquipmentSlot slot, ItemStack item) {
        com.google.common.base.Preconditions.checkArgument(slot != null, "slot");
        com.google.common.base.Preconditions.checkArgument(slot != EquipmentSlot.BODY, "Cannot set body item");
        org.bukkit.entity.ArmorStand self = (org.bukkit.entity.ArmorStand) this;
        switch (slot) {
            case HAND -> self.getEquipment().setItemInMainHand(item);
            case OFF_HAND -> self.getEquipment().setItemInOffHand(item);
            case FEET -> self.getEquipment().setBoots(item);
            case LEGS -> self.getEquipment().setLeggings(item);
            case CHEST -> self.getEquipment().setChestplate(item);
            case HEAD -> self.getEquipment().setHelmet(item);
            default -> throw new UnsupportedOperationException(slot.name());
        }
    }

    default Set<EquipmentSlot> getDisabledSlots() {
        Set<EquipmentSlot> disabled = new HashSet<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (this.isSlotDisabled(slot)) {
                disabled.add(slot);
            }
        }
        return disabled;
    }

    default void setDisabledSlots(EquipmentSlot... slots) {
        int disabled = 0;
        for (EquipmentSlot slot : slots) {
            if (slot == EquipmentSlot.OFF_HAND) {
                continue;
            }
            net.minecraft.world.entity.EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(slot);
            disabled += (1 << nmsSlot.getFilterFlag()) + (1 << (nmsSlot.getFilterFlag() + 8)) + (1 << (nmsSlot.getFilterFlag() + 16));
        }
        ((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).disabledSlots = disabled;
    }

    default void addDisabledSlots(EquipmentSlot... slots) {
        Set<EquipmentSlot> disabled = this.getDisabledSlots();
        Collections.addAll(disabled, slots);
        this.setDisabledSlots(disabled.toArray(new EquipmentSlot[0]));
    }

    default void removeDisabledSlots(EquipmentSlot... slots) {
        Set<EquipmentSlot> disabled = this.getDisabledSlots();
        for (EquipmentSlot slot : slots) {
            disabled.remove(slot);
        }
        this.setDisabledSlots(disabled.toArray(new EquipmentSlot[0]));
    }

    default boolean isSlotDisabled(EquipmentSlot slot) {
        return ((ArmorStandPaperBridge) FusionCoreEntities.handle(this)).fusioncore$isDisabled(CraftEquipmentSlot.getNMS(slot));
    }

    default Rotations getBodyRotations() {
        return FusionCoreEntities.rotations(((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).bodyPose);
    }

    default void setBodyRotations(Rotations rotations) {
        ((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).setBodyPose(FusionCoreEntities.rotations(rotations));
    }

    default Rotations getLeftArmRotations() {
        return FusionCoreEntities.rotations(((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).leftArmPose);
    }

    default void setLeftArmRotations(Rotations rotations) {
        ((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).setLeftArmPose(FusionCoreEntities.rotations(rotations));
    }

    default Rotations getRightArmRotations() {
        return FusionCoreEntities.rotations(((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).rightArmPose);
    }

    default void setRightArmRotations(Rotations rotations) {
        ((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).setRightArmPose(FusionCoreEntities.rotations(rotations));
    }

    default Rotations getLeftLegRotations() {
        return FusionCoreEntities.rotations(((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).leftLegPose);
    }

    default void setLeftLegRotations(Rotations rotations) {
        ((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).setLeftLegPose(FusionCoreEntities.rotations(rotations));
    }

    default Rotations getRightLegRotations() {
        return FusionCoreEntities.rotations(((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).rightLegPose);
    }

    default void setRightLegRotations(Rotations rotations) {
        ((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).setRightLegPose(FusionCoreEntities.rotations(rotations));
    }

    default Rotations getHeadRotations() {
        return FusionCoreEntities.rotations(((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).headPose);
    }

    default void setHeadRotations(Rotations rotations) {
        ((net.minecraft.world.entity.decoration.ArmorStand) FusionCoreEntities.handle(this)).setHeadPose(FusionCoreEntities.rotations(rotations));
    }
}
