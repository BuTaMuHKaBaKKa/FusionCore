package io.izzel.arclight.common.bridge.core.world.entity.item;

public interface ItemEntityBridge {

    default void bridge$forge$optimization$discardItemEntity() {}

    /** The age at which this item despawns: spigot.yml item-despawn-rate (NeoForge: the entity's lifespan). */
    default int bridge$fusioncore$despawnAge() {
        return 6000;
    }
}
