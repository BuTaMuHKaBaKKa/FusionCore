package io.izzel.arclight.neoforge.mixin.core.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCoreShearing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.IShearable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * {@code PlayerShearEntityEvent} for shearing with the item. Fusion Core: fired once the entity is shearable (Arclight fired it
 * before {@code isShearable}, also for sheep already sheared), with Paper's drops (patch 0916), see {@link FusionCoreShearing}.
 */
@Mixin(ShearsItem.class)
public abstract class ShearsItemMixin_NeoForge {

    @WrapOperation(method = "interactLivingEntity", at = @At(value = "INVOKE", remap = false, target = "Lnet/neoforged/neoforge/common/IShearable;isShearable(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean arclight$onShear(IShearable target, Player player, ItemStack item, Level level, BlockPos pos, Operation<Boolean> original,
                                     ItemStack stack, Player playerIn, LivingEntity entity, InteractionHand hand) {
        return original.call(target, player, item, level, pos) && FusionCoreShearing.playerShear(playerIn, entity, stack, hand);
    }

    @WrapOperation(method = "interactLivingEntity", at = @At(value = "INVOKE", remap = false, target = "Lnet/neoforged/neoforge/common/IShearable;onSheared(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Ljava/util/List;"))
    private List<ItemStack> fusioncore$shearDrops(IShearable target, Player player, ItemStack item, Level level, BlockPos pos, Operation<List<ItemStack>> original) {
        if (!(target instanceof net.minecraft.world.entity.Entity entity)) {
            return original.call(target, player, item, level, pos);
        }
        return FusionCoreShearing.shear(entity, () -> original.call(target, player, item, level, pos));
    }
}
