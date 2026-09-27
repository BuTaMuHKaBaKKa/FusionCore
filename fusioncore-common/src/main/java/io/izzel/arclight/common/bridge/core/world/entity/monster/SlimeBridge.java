package io.izzel.arclight.common.bridge.core.world.entity.monster;

/** Fusion Core: Paper's {@code Slime#canWander} / {@code setWander} (Paper patch 0232). */
public interface SlimeBridge {

    boolean bridge$canWander();

    void bridge$setWander(boolean canWander);
}
