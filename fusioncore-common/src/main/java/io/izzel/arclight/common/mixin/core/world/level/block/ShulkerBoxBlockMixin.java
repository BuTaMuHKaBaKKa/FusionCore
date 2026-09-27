package io.izzel.arclight.common.mixin.core.world.level.block;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.world.LootableDataBridge;
import io.izzel.arclight.common.bridge.core.world.RandomizableContainerBridge;
import io.izzel.arclight.common.mod.util.FusionCoreLootables;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/** Fusion Core: shulker boxes with replenishable loot drop their loot table as Paper does (patch 0095). */
@Mixin(ShulkerBoxBlock.class)
public abstract class ShulkerBoxBlockMixin {

    @WrapOperation(method = "playerWillDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/ShulkerBoxBlockEntity;unpackLootTable(Lnet/minecraft/world/entity/player/Player;)V"))
    private void fusioncore$forceClearLootTable(ShulkerBoxBlockEntity box, Player player, Operation<Void> original) {
        ((RandomizableContainerBridge) box).fusioncore$unpackLootTable(player, true);
    }

    @WrapMethod(method = "getDrops")
    private List<ItemStack> fusioncore$dropsWithoutUsedLootTable(BlockState state, LootParams.Builder builder, Operation<List<ItemStack>> original) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ShulkerBoxBlockEntity box) {
            FusionCoreLootables.Data data = ((LootableDataBridge) box).fusioncore$lootableData();
            if (data != null && (data.getLastFill() != -1 || !FusionCoreLootables.config().isRetainUnlootedShulkerBoxLootTableOnNonPlayerBreak())) {
                ResourceKey<LootTable> lootTable = box.getLootTable();
                box.setLootTable(null);
                try {
                    return original.call(state, builder);
                } finally {
                    box.setLootTable(lootTable);
                }
            }
        }
        return original.call(state, builder);
    }
}
