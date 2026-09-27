package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's trident API (patches 0557, 0674).
 */
@Mixin(value = org.bukkit.entity.Trident.class, remap = false)
public interface TridentMixin {

    default boolean hasGlint() {
        net.minecraft.world.entity.projectile.ThrownTrident h = FusionCoreEntities.nms(this);
        return h.isFoil();
    }

    default void setGlint(boolean glint) {
        net.minecraft.world.entity.projectile.ThrownTrident h = FusionCoreEntities.nms(this);
        h.getEntityData().set(net.minecraft.world.entity.projectile.ThrownTrident.ID_FOIL, glint);
    }

    default int getLoyaltyLevel() {
        net.minecraft.world.entity.projectile.ThrownTrident h = FusionCoreEntities.nms(this);
        return h.getEntityData().get(net.minecraft.world.entity.projectile.ThrownTrident.ID_LOYALTY);
    }

    default void setLoyaltyLevel(int loyaltyLevel) {
        com.google.common.base.Preconditions.checkArgument(loyaltyLevel >= 0 && loyaltyLevel <= 127, "The loyalty level has to be between 0 and 127");
        net.minecraft.world.entity.projectile.ThrownTrident h = FusionCoreEntities.nms(this);
        h.getEntityData().set(net.minecraft.world.entity.projectile.ThrownTrident.ID_LOYALTY, (byte) loyaltyLevel);
    }

    default boolean hasDealtDamage() {
        net.minecraft.world.entity.projectile.ThrownTrident h = FusionCoreEntities.nms(this);
        return h.dealtDamage;
    }

    default void setHasDealtDamage(boolean hasDealtDamage) {
        net.minecraft.world.entity.projectile.ThrownTrident h = FusionCoreEntities.nms(this);
        h.dealtDamage = hasDealtDamage;
    }
}
