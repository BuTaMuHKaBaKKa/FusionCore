package io.izzel.arclight.common.mixin.core.world.inventory;

import io.izzel.arclight.common.bridge.core.world.entity.player.PlayerBridge;
import io.izzel.arclight.common.bridge.core.world.inventory.AnvilMenuBridge;
import io.izzel.arclight.common.bridge.core.world.inventory.ContainerLevelAccessBridge;
import io.izzel.arclight.common.mod.server.world.inventory.ArclightAnvilView;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.inventory.view.CraftAnvilView;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import io.izzel.arclight.common.mod.util.FusionCorePrepareResult;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenuMixin implements AnvilMenuBridge {

    // @formatter:off
    @Shadow @Final public DataSlot cost;
    @Shadow public int repairItemCountCost;
    @Shadow public String itemName;
    @Shadow public static int calculateIncreasedRepairCost(int oldRepairCost) { return 0; }
    // @formatter:on

    public int cancelThisBySettingCostToMaximum = 40;
    public int maximumRenameCostThreshold = 40;
    public int maximumAllowedRenameCost = 39;
    public int maximumRepairCost = 40;

    private CraftAnvilView bukkitEntity;

    private boolean arclight$zeroCostAllowed = false;

    @Override
    public void arclight$allowZeroCost() {
        arclight$zeroCostAllowed = true;
    }

    @Override
    public boolean arclight$isZeroCostAllowed() {
        return arclight$zeroCostAllowed;
    }

    @Decorate(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void arclight$prepareAnvilEvent(ResultContainer instance, int i, ItemStack itemStack) throws Throwable {
        arclight$zeroCostAllowed = false;
        final CraftAnvilView craft = getBukkitView();
        // Fusion Core: PrepareAnvilEvent is fired after createResult as Paper's PrepareResultEvent (FusionCorePrepareResult)
        if (craft instanceof ArclightAnvilView) {
            DecorationOps.callsite().invoke(instance, i, itemStack);
        } else {
            craft.getTopInventory().setItem(AnvilMenu.RESULT_SLOT, CraftItemStack.asCraftMirror(itemStack));
        }
    }

    @Decorate(method = "mayPickup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;get()I"))
    private int arclight$tryAllowZeroCost(DataSlot instance) throws Throwable {
        final int value = (int) DecorationOps.callsite().invoke(instance);
        if (value == 0 && arclight$zeroCostAllowed) {
            return 1;
        }
        return value;
    }

    @Inject(method = "mayPickup", at = @At("RETURN"), cancellable = true)
    private void arclight$considerFlag(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValueZ() && hasItem);
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;broadcastChanges()V"))
    private void arclight$sync(CallbackInfo ci) {
        this.sendAllDataToRemote();
    }

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40), require = 0)
    private int arclight$maximumRepairCost(int i) {
        return i - 40 + maximumRepairCost;
    }

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 39), require = 0)
    private int arclight$maximumRepairCost2(int i) {
        return i - 40 + maximumRepairCost;
    }

    @Override
    public CraftAnvilView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        var inventory = new CraftInventoryAnvil(
            ((ContainerLevelAccessBridge) this.access).bridge$getLocation(), this.inputSlots, this.resultSlots);
        bukkitEntity = new ArclightAnvilView(((PlayerBridge) this.player).bridge$getBukkitEntity(), inventory, (AnvilMenu) (Object) this);
        return bukkitEntity;
    }

    // Fusion Core: Paper's PrepareResultEvent after renaming
    @Inject(method = "setItemName", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/inventory/AnvilMenu;createResult()V"))
    private void fusioncore$prepareResultOnRename(String name, CallbackInfoReturnable<Boolean> cir) {
        FusionCorePrepareResult.call((AnvilMenu) (Object) this, AnvilMenu.RESULT_SLOT);
    }

    // Fusion Core: Paper's AnvilDamagedEvent (Paper patch 0220) in onTake's damage lambda (static on NeoForge, matched by
    // descriptor). Cancelled: the anvil stays and makes no sound, as in Paper; a different damage state is applied here.
    @Inject(method = "*(Lnet/minecraft/world/entity/player/Player;FLnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", cancellable = true,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/AnvilBlock;damage(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static void fusioncore$anvilDamagedEvent(Player player, float breakChance, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, CallbackInfo ci,
                                                   @com.llamalad7.mixinextras.sugar.Local(ordinal = 0) net.minecraft.world.level.block.state.BlockState state) {
        if (io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.anvilDamaged(player, level, pos, state)) {
            ci.cancel();
        }

    }

    // Fusion Core: Paper patch 1066 "Add AnvilView#bypassEnchantmentLevelRestriction": merged levels above the maximum are kept.
    @org.spongepowered.asm.mixin.Unique private boolean fusioncore$bypassEnchantmentLevelRestriction;

    @Override
    public boolean fusioncore$bypassEnchantmentLevelRestriction() {
        return this.fusioncore$bypassEnchantmentLevelRestriction;
    }

    @Override
    public void fusioncore$setBypassEnchantmentLevelRestriction(boolean bypass) {
        this.fusioncore$bypassEnchantmentLevelRestriction = bypass;
    }

    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method = "createResult", at = @At(value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"))
    private int fusioncore$bypassMaxLevel(int maxLevel) {
        return this.fusioncore$bypassEnchantmentLevelRestriction ? Integer.MAX_VALUE : maxLevel;
    }
}
