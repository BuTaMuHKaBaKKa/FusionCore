package io.izzel.arclight.common.bridge.core.world.entity.monster;

import io.izzel.arclight.common.bridge.core.world.entity.MobBridge;

public interface CreeperBridge extends MobBridge {

    void bridge$setPowered(boolean power);

    /** Fusion Core: Paper's {@code Creeper#setIgnited}, firing CreeperIgniteEvent. */
    void bridge$setIgnited(boolean ignited);
}
