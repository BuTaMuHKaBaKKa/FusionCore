package io.izzel.arclight.common.mixin.core.world.entity.item;

import io.izzel.arclight.common.bridge.core.world.entity.item.ItemEntityPaperBridge;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Fusion Core: Paper's {@code Item#setCanMobPickup} (patch 0132; the flag is checked in {@code MobMixin#aiStep}, not saved,
 * as in Paper) and item entity health (patch 0639).
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityPaperMixin implements ItemEntityPaperBridge {

    // @formatter:off
    @Shadow private int health;
    // @formatter:on

    @Unique private boolean fusioncore$canMobPickup = true;

    @Override
    public boolean fusioncore$canMobPickup() {
        return this.fusioncore$canMobPickup;
    }

    @Override
    public void fusioncore$setCanMobPickup(boolean canMobPickup) {
        this.fusioncore$canMobPickup = canMobPickup;
    }

    @Override
    public int fusioncore$health() {
        return this.health;
    }

    @Override
    public void fusioncore$setHealth(int health) {
        this.health = health;
    }
}
