package io.izzel.arclight.common.mixin.bukkit.event;

import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's deprecated names for Spigot's consumable methods of {@code EntityShootBowEvent} (API patch 0108). */
@Mixin(value = EntityShootBowEvent.class, remap = false)
public abstract class EntityShootBowEventMixin {

    @Shadow public abstract void setConsumeItem(boolean consumeItem);
    @Shadow public abstract boolean shouldConsumeItem();
    @Shadow public abstract ItemStack getConsumable();

    public void setConsumeArrow(boolean consumeArrow) {
        this.setConsumeItem(consumeArrow);
    }

    public boolean getConsumeArrow() {
        return this.shouldConsumeItem();
    }

    public ItemStack getArrowItem() {
        return this.getConsumable();
    }
}
