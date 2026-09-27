package io.izzel.arclight.common.bridge.core.world.entity;

/** Fusion Core: Paper's armor stand state and internals (patches 0118, 0217, 0219), see {@code ArmorStandPaperMixin}. */
public interface ArmorStandPaperBridge {

    boolean fusioncore$canTick();

    void fusioncore$setCanTick(boolean canTick);

    boolean fusioncore$canMove();

    void fusioncore$setCanMove(boolean canMove);

    boolean fusioncore$isDisabled(net.minecraft.world.entity.EquipmentSlot slot);
}
