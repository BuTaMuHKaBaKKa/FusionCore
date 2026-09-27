package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.entity.CraftFirework;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

/** Fusion Core: firework meta read from and written to the entity's current item (patch 0674). */
@Mixin(value = CraftFirework.class, remap = false)
public abstract class CraftFireworkMixin {

    @Shadow @Final private Random random;

    @Shadow public abstract FireworkRocketEntity getHandle();

    /**
     * @author Fusion Core
     * @reason Paper: the meta of the entity's current item
     */
    @Overwrite
    public FireworkMeta getFireworkMeta() {
        return (FireworkMeta) CraftItemStack.getItemMeta(this.getHandle().getEntityData().get(FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM));
    }

    /**
     * @author Fusion Core
     * @reason Paper: applied to the entity's current item
     */
    @Overwrite
    public void setFireworkMeta(FireworkMeta meta) {
        ItemStack item = this.getHandle().getItem();
        CraftItemStack.setItemMeta(item, meta);
        this.getHandle().getEntityData().set(FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM, item);
        // copied from the FireworkRocketEntity constructor: lifetime from the power, as Spigot does
        this.getHandle().lifetime = 10 * (1 + meta.getPower()) + this.random.nextInt(6) + this.random.nextInt(7);
        ((io.izzel.arclight.common.bridge.core.network.syncher.SynchedEntityDataBridge) this.getHandle().getEntityData()).bridge$markDirty(FireworkRocketEntity.DATA_ID_FIREWORKS_ITEM);
    }
}
