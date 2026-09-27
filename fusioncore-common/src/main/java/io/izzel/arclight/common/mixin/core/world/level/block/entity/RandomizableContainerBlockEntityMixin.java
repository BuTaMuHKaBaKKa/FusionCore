package io.izzel.arclight.common.mixin.core.world.level.block.entity;

import io.izzel.arclight.common.bridge.core.world.LootableDataBridge;
import io.izzel.arclight.common.mod.util.FusionCoreLootables;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's replenish data on loot containers (patch 0095), see {@code RandomizableContainerMixin}. */
@Mixin(RandomizableContainerBlockEntity.class)
public abstract class RandomizableContainerBlockEntityMixin implements LootableDataBridge {

    private final FusionCoreLootables.Data fusioncore$lootData = new FusionCoreLootables.Data();

    @Override
    public FusionCoreLootables.Data fusioncore$lootableData() {
        return this.fusioncore$lootData;
    }
}
