package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's firework API (patch 0674): the item itself (any item, the effects of the current one kept), ticks flown and to detonate; {@code CraftFireworkMixin} makes the firework meta read the entity's item as Paper does.
 */
@Mixin(value = org.bukkit.entity.Firework.class, remap = false)
public interface FireworkMixin {

    /** Paper patch 0121: who launched the rocket (crossbow shooter, player placing or boosting), {@code null} otherwise. */
    default java.util.UUID getSpawningEntity() {
        return ((io.izzel.arclight.common.bridge.core.world.entity.api.FireworkApiBridge) FusionCoreEntities.handle(this)).fusioncore$spawningEntity();
    }

    default org.bukkit.entity.LivingEntity getBoostedEntity() {
        return ((org.bukkit.entity.Firework) this).getAttachedTo();
    }

    default org.bukkit.inventory.ItemStack getItem() {
        net.minecraft.world.entity.projectile.FireworkRocketEntity h = FusionCoreEntities.nms(this);
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asBukkitCopy(h.getItem());
    }

    default void setItem(org.bukkit.inventory.ItemStack itemStack) {
        net.minecraft.world.entity.projectile.FireworkRocketEntity h = FusionCoreEntities.nms(this);
        org.bukkit.inventory.meta.FireworkMeta meta = ((org.bukkit.entity.Firework) this).getFireworkMeta();
        net.minecraft.world.item.ItemStack item = itemStack == null ? io.izzel.arclight.common.mixin.core.world.entity.api.FireworkRocketEntityApiInvoker.fusioncore$getDefaultItem() : org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(itemStack);
        org.bukkit.craftbukkit.v.inventory.CraftItemStack.setItemMeta(item, meta);
        h.getEntityData().set(net.minecraft.world.entity.projectile.FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM, item);
    }

    default int getTicksFlown() {
        net.minecraft.world.entity.projectile.FireworkRocketEntity h = FusionCoreEntities.nms(this);
        return h.life;
    }

    default void setTicksFlown(int ticks) {
        net.minecraft.world.entity.projectile.FireworkRocketEntity h = FusionCoreEntities.nms(this);
        h.life = ticks;
    }

    default int getTicksToDetonate() {
        net.minecraft.world.entity.projectile.FireworkRocketEntity h = FusionCoreEntities.nms(this);
        return h.lifetime;
    }

    default void setTicksToDetonate(int ticks) {
        net.minecraft.world.entity.projectile.FireworkRocketEntity h = FusionCoreEntities.nms(this);
        h.lifetime = ticks;
    }
}
