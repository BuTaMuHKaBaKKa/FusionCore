package io.izzel.arclight.common.mixin.core.world.entity.vehicle;

import io.izzel.arclight.common.bridge.core.world.LootableDataBridge;
import io.izzel.arclight.common.mod.util.FusionCoreLootables;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
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

/** Fusion Core: Paper's replenishable lootables on container minecarts and chest boats (patch 0095). */
@Mixin(ContainerEntity.class)
public interface ContainerEntityMixin extends LootableDataBridge {

    // @formatter:off
    @Shadow @Nullable ResourceKey<LootTable> getLootTable();
    @Shadow void setLootTable(@Nullable ResourceKey<LootTable> lootTable);
    @Shadow long getLootTableSeed();
    @Shadow void setLootTableSeed(long seed);
    @Shadow NonNullList<ItemStack> getItemStacks();
    @Shadow void clearItemStacks();
    @Shadow Level level();
    @Shadow Vec3 position();
    // @formatter:on

    @Override
    default FusionCoreLootables.Data fusioncore$lootableData() {
        return null;
    }

    /**
     * @author Fusion Core
     * @reason Paper saves its lootable data with the loot table and always saves the items
     */
    @Overwrite
    default void addChestVehicleSaveData(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        FusionCoreLootables.Data data = this.fusioncore$lootableData();
        if (this.getLootTable() != null) {
            tag.putString("LootTable", this.getLootTable().location().toString());
            if (data != null) {
                data.saveNbt(tag);
            }
            if (this.getLootTableSeed() != 0L) {
                tag.putLong("LootTableSeed", this.getLootTableSeed());
            }
            if (data == null) {
                return;
            }
        }
        ContainerHelper.saveAllItems(tag, this.getItemStacks(), levelRegistry);
    }

    /**
     * @author Fusion Core
     * @reason Paper loads its lootable data with the loot table and always loads the items
     */
    @Overwrite
    default void readChestVehicleSaveData(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        this.clearItemStacks();
        FusionCoreLootables.Data data = this.fusioncore$lootableData();
        if (tag.contains("LootTable", 8)) {
            this.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(tag.getString("LootTable"))));
            if (data != null && this.getLootTable() != null) {
                data.loadNbt(tag);
            }
            this.setLootTableSeed(tag.getLong("LootTableSeed"));
            if (data == null) {
                return;
            }
        }
        ContainerHelper.loadAllItems(tag, this.getItemStacks(), levelRegistry);
    }

    /**
     * @author Fusion Core
     * @reason Paper's replenish checks
     */
    @Overwrite
    default void unpackChestVehicleLootTable(@Nullable Player player) {
        MinecraftServer server = this.level().getServer();
        FusionCoreLootables.Data data = this.fusioncore$lootableData();
        boolean unpack = data == null
            ? this.getLootTable() != null && server != null
            : server != null && data.shouldReplenish((ContainerEntity) this, FusionCoreLootables.ENTITY, player);
        if (unpack) {
            LootTable lootTable = server.reloadableRegistries().getLootTable(this.getLootTable());
            if (player != null) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer) player, this.getLootTable());
            }
            if (data == null || data.shouldClearLootTable((ContainerEntity) this, FusionCoreLootables.ENTITY, player)) {
                this.setLootTable(null);
            }
            LootParams.Builder builder = new LootParams.Builder((ServerLevel) this.level()).withParameter(LootContextParams.ORIGIN, this.position());
            if ((Object) this instanceof AbstractMinecartContainer container) {
                builder.withParameter(LootContextParams.ATTACKING_ENTITY, container);
            }
            if (player != null) {
                builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }
            lootTable.fill((ContainerEntity) this, builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());
        }
    }
}
