package io.izzel.arclight.neoforge.mixin.core.core.dispenser;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCoreShearing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.IShearable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * Fusion Core: CraftBukkit's {@code BlockShearEntityEvent} for a dispenser shearing an entity, with Paper's drops (patch 0916).
 * Arclight did not fire it on NeoForge, where the dispenser goes through {@code IShearable}; see {@link FusionCoreShearing}.
 */
@Mixin(ShearsDispenseItemBehavior.class)
public abstract class ShearsDispenseItemBehaviorMixin_NeoForge {

    @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/dispenser/ShearsDispenseItemBehavior;tryShearLivingEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean fusioncore$dispenser(ServerLevel level, BlockPos pos, ItemStack stack, Operation<Boolean> original, BlockSource source, ItemStack dispensed) {
        boolean[] sheared = new boolean[1];
        FusionCoreShearing.withDispenser(source.pos(), () -> sheared[0] = original.call(level, pos, stack));
        return sheared[0];
    }

    @WrapOperation(method = "tryShearLivingEntity", at = @At(value = "INVOKE", remap = false, target = "Lnet/neoforged/neoforge/common/IShearable;isShearable(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean fusioncore$shearEvent(IShearable target, Player player, ItemStack item, Level level, BlockPos pos, Operation<Boolean> original,
                                               ServerLevel serverLevel, BlockPos targetPos, ItemStack stack) {
        return original.call(target, player, item, level, pos) && (!(target instanceof Entity entity) || FusionCoreShearing.dispenserShear(serverLevel, entity, stack));
    }

    @WrapOperation(method = "tryShearLivingEntity", at = @At(value = "INVOKE", remap = false, target = "Lnet/neoforged/neoforge/common/IShearable;onSheared(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Ljava/util/List;"))
    private static List<ItemStack> fusioncore$shearDrops(IShearable target, Player player, ItemStack item, Level level, BlockPos pos, Operation<List<ItemStack>> original) {
        if (!(target instanceof net.minecraft.world.entity.Entity entity)) {
            return original.call(target, player, item, level, pos);
        }
        return FusionCoreShearing.shear(entity, () -> original.call(target, player, item, level, pos));
    }
}
