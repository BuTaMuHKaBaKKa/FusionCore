package io.izzel.arclight.common.bridge.core.world;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: Paper's {@code RandomizableContainer#unpackLootTable(Player, boolean)} (patch 0095). */
public interface RandomizableContainerBridge extends LootableDataBridge {

    /** Fills the container from its loot table; {@code forceClear} drops the loot table even if it could refill. */
    void fusioncore$unpackLootTable(@Nullable Player player, boolean forceClear);
}
