package io.izzel.arclight.common.bridge.core.world.entity.monster;

import net.minecraft.world.item.ItemStack;

/** Fusion Core: Paper's Witch API (Paper patch 0256 "Add more Witch API"). */
public interface WitchBridge {

    int bridge$getUsingTime();

    void bridge$setUsingTime(int usingTime);

    /** Paper's {@code Witch#setDrinkingPotion}: the witch starts drinking, after {@code WitchReadyPotionEvent}. */
    void bridge$setDrinkingPotion(ItemStack potion);
}
