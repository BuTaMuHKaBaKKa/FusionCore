package io.izzel.arclight.common.bridge.core.world.entity.npc;

import net.minecraft.core.BlockPos;

/** Fusion Core: Paper's WanderingTrader API (Paper patches 0523 "Add more WanderingTrader API", 0557 "Missing Entity API"). */
public interface WanderingTraderBridge {

    boolean bridge$canDrinkPotion();

    void bridge$setCanDrinkPotion(boolean canDrink);

    boolean bridge$canDrinkMilk();

    void bridge$setCanDrinkMilk(boolean canDrink);

    BlockPos bridge$getWanderTarget();
}
