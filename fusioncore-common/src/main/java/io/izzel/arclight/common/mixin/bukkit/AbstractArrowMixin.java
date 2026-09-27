package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's arrow API: pickup rule (patch 0240), pickup item, lifetime and hit sound (0674), shooter without resetting the pickup status (1035).
 */
@Mixin(value = org.bukkit.entity.AbstractArrow.class, remap = false)
public interface AbstractArrowMixin {

    @Deprecated
    default org.bukkit.entity.AbstractArrow$PickupRule getPickupRule() {
        return org.bukkit.entity.AbstractArrow$PickupRule.valueOf(((org.bukkit.entity.AbstractArrow) this).getPickupStatus().name());
    }

    @Deprecated
    default void setPickupRule(org.bukkit.entity.AbstractArrow$PickupRule rule) {
        ((org.bukkit.entity.AbstractArrow) this).setPickupStatus(org.bukkit.entity.AbstractArrow.PickupStatus.valueOf(rule.name()));
    }

    default org.bukkit.inventory.ItemStack getItemStack() {
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(((io.izzel.arclight.common.mixin.core.world.entity.api.AbstractArrowApiInvoker) FusionCoreEntities.handle(this)).fusioncore$getPickupItem());
    }

    default void setItemStack(org.bukkit.inventory.ItemStack stack) {
        com.google.common.base.Preconditions.checkArgument(stack != null, "ItemStack cannot be null");
        ((io.izzel.arclight.common.mixin.core.world.entity.api.AbstractArrowApiInvoker) FusionCoreEntities.handle(this)).fusioncore$setPickupItemStack(org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(stack));
    }

    default void setLifetimeTicks(int ticks) {
        net.minecraft.world.entity.projectile.AbstractArrow h = FusionCoreEntities.nms(this);
        h.life = ticks;
    }

    default int getLifetimeTicks() {
        net.minecraft.world.entity.projectile.AbstractArrow h = FusionCoreEntities.nms(this);
        return h.life;
    }

    default org.bukkit.Sound getHitSound() {
        net.minecraft.world.entity.projectile.AbstractArrow h = FusionCoreEntities.nms(this);
        return org.bukkit.craftbukkit.v.CraftSound.minecraftToBukkit(h.soundEvent);
    }

    default void setHitSound(org.bukkit.Sound sound) {
        net.minecraft.world.entity.projectile.AbstractArrow h = FusionCoreEntities.nms(this);
        h.setSoundEvent(org.bukkit.craftbukkit.v.CraftSound.bukkitToMinecraft(sound));
    }

    /** Paper's {@code setOwner(entity, false)} skips the pickup update that vanilla's owner setter does; the status is kept here. */
    default void setShooter(org.bukkit.projectiles.ProjectileSource source, boolean resetPickupStatus) {
        net.minecraft.world.entity.projectile.AbstractArrow h = FusionCoreEntities.nms(this);
        net.minecraft.world.entity.projectile.AbstractArrow.Pickup pickup = h.pickup;
        ((org.bukkit.entity.AbstractArrow) this).setShooter(source);
        if (!resetPickupStatus) {
            h.pickup = pickup;
        }
    }
}
