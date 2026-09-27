package io.izzel.arclight.common.bridge.core.world.entity.monster;

import java.util.UUID;

public interface PhantomBridge {

    UUID bridge$getSpawningEntity();

    void bridge$setSpawningEntity(UUID entity);
}
