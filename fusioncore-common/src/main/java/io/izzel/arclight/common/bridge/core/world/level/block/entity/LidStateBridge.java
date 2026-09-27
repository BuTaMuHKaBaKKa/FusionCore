package io.izzel.arclight.common.bridge.core.world.level.block.entity;

/**
 * Fusion Core: CraftBukkit's "opened by the API" flag ({@code ContainerOpenersCounter#opened},
 * {@code ShulkerBoxBlockEntity#opened}) for Paper's {@code Lidded#isOpen} (patch 0541).
 */
public interface LidStateBridge {

    boolean fusioncore$lidOpened();
}
