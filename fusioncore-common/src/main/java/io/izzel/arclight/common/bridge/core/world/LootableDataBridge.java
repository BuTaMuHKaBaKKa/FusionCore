package io.izzel.arclight.common.bridge.core.world;

import io.izzel.arclight.common.mod.util.FusionCoreLootables;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: Paper's {@code lootableData()} (patch 0095); null unless the holder keeps replenish data. */
public interface LootableDataBridge {

    @Nullable FusionCoreLootables.Data fusioncore$lootableData();
}
