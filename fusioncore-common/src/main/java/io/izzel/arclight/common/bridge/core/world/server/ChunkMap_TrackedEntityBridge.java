package io.izzel.arclight.common.bridge.core.world.server;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;

public interface ChunkMap_TrackedEntityBridge {

    ServerEntity bridge$getServerEntity();

    Entity bridge$getEntity();

    SectionPos bridge$getLastSectionPos();

    void bridge$setLastSectionPos(SectionPos pos);

    /** Fusion Core: the connections that see the entity now */
    java.util.Set<net.minecraft.server.network.ServerPlayerConnection> fusioncore$seenBy();

    /** Fusion Core: vanilla's getEffectiveRange (the largest tracking range of the entity and its passengers) */
    int fusioncore$effectiveRange();
}
