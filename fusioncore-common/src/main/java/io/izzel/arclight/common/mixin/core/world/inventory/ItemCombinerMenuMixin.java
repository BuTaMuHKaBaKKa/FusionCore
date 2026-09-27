package io.izzel.arclight.common.mixin.core.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ResultContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.izzel.arclight.common.mod.util.FusionCorePrepareResult;

@Mixin(ItemCombinerMenu.class)
public abstract class ItemCombinerMenuMixin extends AbstractContainerMenuMixin {

    // @formatter:off
    @Shadow @Final protected ContainerLevelAccess access;
    @Shadow @Final @Mutable protected Container inputSlots;
    @Shadow @Final protected ResultContainer resultSlots;
    @Shadow @Final protected Player player;
    // @formatter:on

    @Inject(method = "stillValid", cancellable = true, at = @At("HEAD"))
    private void arclight$unreachable(Player playerIn, CallbackInfoReturnable<Boolean> cir) {
        if (!bridge$isCheckReachable()) cir.setReturnValue(true);
    }

    @Shadow @Final private int resultSlotIndex;

    // Fusion Core: Paper's PrepareResultEvent for anvil and smithing table
    @Inject(method = "slotsChanged", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/inventory/ItemCombinerMenu;createResult()V"))
    private void fusioncore$prepareResult(Container container, CallbackInfo ci) {
        FusionCorePrepareResult.call((ItemCombinerMenu) (Object) this, this.resultSlotIndex);
    }
}
