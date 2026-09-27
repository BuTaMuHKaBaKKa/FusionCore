package io.izzel.arclight.common.mixin.core.world;

import io.izzel.arclight.common.bridge.core.world.LootableDataBridge;
import io.izzel.arclight.common.bridge.core.world.RandomizableContainerBridge;
import io.izzel.arclight.common.mod.util.FusionCoreLootables;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's replenishable lootables on block containers (patch 0095). */
@Mixin(RandomizableContainer.class)
public interface RandomizableContainerMixin extends RandomizableContainerBridge {

    // @formatter:off
    @Shadow @Nullable ResourceKey<LootTable> getLootTable();
    @Shadow void setLootTable(@Nullable ResourceKey<LootTable> lootTable);
    @Shadow long getLootTableSeed();
    @Shadow void setLootTableSeed(long seed);
    @Shadow @Nullable Level getLevel();
    @Shadow BlockPos getBlockPos();
    // @formatter:on

    @Override
    default FusionCoreLootables.Data fusioncore$lootableData() {
        return null;
    }

    /**
     * @author Fusion Core
     * @reason Paper's lootable data is loaded with the loot table, and items are loaded too when the holder has it
     */
    @Overwrite
    default boolean tryLoadLootTable(CompoundTag tag) {
        if (tag.contains("LootTable", 8)) {
            this.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(tag.getString("LootTable"))));
            FusionCoreLootables.Data data = this.fusioncore$lootableData();
            if (data != null && this.getLootTable() != null) {
                data.loadNbt(tag);
            }
            if (tag.contains("LootTableSeed", 4)) {
                this.setLootTableSeed(tag.getLong("LootTableSeed"));
            } else {
                this.setLootTableSeed(0L);
            }
            return data == null;
        } else {
            return false;
        }
    }

    /**
     * @author Fusion Core
     * @reason Paper's lootable data is saved with the loot table, and items are saved too when the holder has it
     */
    @Overwrite
    default boolean trySaveLootTable(CompoundTag tag) {
        ResourceKey<LootTable> resourceKey = this.getLootTable();
        if (resourceKey == null) {
            return false;
        } else {
            tag.putString("LootTable", resourceKey.location().toString());
            FusionCoreLootables.Data data = this.fusioncore$lootableData();
            if (data != null) {
                data.saveNbt(tag);
            }
            long seed = this.getLootTableSeed();
            if (seed != 0L) {
                tag.putLong("LootTableSeed", seed);
            }
            return data == null;
        }
    }

    /**
     * @author Fusion Core
     * @reason Paper's replenish checks
     */
    @Overwrite
    default void unpackLootTable(@Nullable Player player) {
        this.fusioncore$unpackLootTable(player, false);
    }

    @Override
    default void fusioncore$unpackLootTable(@Nullable Player player, boolean forceClear) {
        Level level = this.getLevel();
        BlockPos blockPos = this.getBlockPos();
        ResourceKey<LootTable> resourceKey = this.getLootTable();
        if (resourceKey != null && level != null && level.getServer() != null) {
            FusionCoreLootables.Data data = this.fusioncore$lootableData();
            if (data != null && !data.shouldReplenish((RandomizableContainer) this, FusionCoreLootables.CONTAINER, player)) {
                if (forceClear) {
                    this.setLootTable(null);
                }
                return;
            }
            LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(resourceKey);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer) player, resourceKey);
            }
            if (forceClear || data == null || data.shouldClearLootTable((RandomizableContainer) this, FusionCoreLootables.CONTAINER, player)) {
                this.setLootTable(null);
            }
            LootParams.Builder builder = new LootParams.Builder((ServerLevel) level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos));
            if (player != null) {
                builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }
            lootTable.fill((RandomizableContainer) this, builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());
        }
    }
}
