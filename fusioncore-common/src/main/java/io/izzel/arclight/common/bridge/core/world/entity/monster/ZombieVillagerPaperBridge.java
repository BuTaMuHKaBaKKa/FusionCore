package io.izzel.arclight.common.bridge.core.world.entity.monster;

/** Fusion Core: Paper's conversion without the entity event (patch 0557, {@code startConverting(uuid, delay, false)}). */
public interface ZombieVillagerPaperBridge {

    void fusioncore$startConverting(java.util.UUID starter, int delay, boolean broadcastEntityEvent);
}
