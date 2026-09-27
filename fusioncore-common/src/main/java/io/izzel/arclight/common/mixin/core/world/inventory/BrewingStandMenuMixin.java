package io.izzel.arclight.common.mixin.core.world.inventory;

import io.izzel.arclight.common.bridge.core.world.entity.player.PlayerBridge;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryBrewer;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v.inventory.view.CraftBrewingStandView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandMenu.class)
public abstract class BrewingStandMenuMixin extends AbstractContainerMenuMixin implements io.izzel.arclight.common.bridge.core.world.inventory.BrewingStandMenuBridge {

    // Fusion Core start: Paper's recipe brew time (Paper patch 1061 "Add recipeBrewTime"). Paper adds a third value to the
    // menu's ContainerData; that would break mods that build a BrewingStandMenu with vanilla's two values, so the time is
    // kept on the brewing stand (or on the menu for a plugin-made one) and only the brew progress sent to the client is
    // scaled to vanilla's 400 ticks, as Paper's data slot does.
    @org.spongepowered.asm.mixin.Unique private int fusioncore$recipeBrewTime = 400;

    @Override
    public int bridge$getRecipeBrewTime() {
        return this.brewingStand instanceof io.izzel.arclight.common.bridge.core.world.level.block.entity.BrewingStandBlockEntityBridge stand
            ? stand.bridge$getRecipeBrewTime() : this.fusioncore$recipeBrewTime;
    }

    @Override
    public void bridge$setRecipeBrewTime(int recipeBrewTime) {
        if (this.brewingStand instanceof io.izzel.arclight.common.bridge.core.world.level.block.entity.BrewingStandBlockEntityBridge stand) {
            stand.bridge$setRecipeBrewTime(recipeBrewTime);
        } else {
            this.fusioncore$recipeBrewTime = recipeBrewTime;
        }
    }

    @org.spongepowered.asm.mixin.injection.ModifyArg(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/BrewingStandMenu;addDataSlots(Lnet/minecraft/world/inventory/ContainerData;)V"))
    private ContainerData fusioncore$scaledBrewTime(ContainerData data) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                int value = data.get(index);
                int recipeBrewTime = bridge$getRecipeBrewTime();
                return index == 0 && recipeBrewTime != 400 && recipeBrewTime > 0 ? 400 * value / recipeBrewTime : value;
            }

            @Override
            public void set(int index, int value) {
                data.set(index, value);
            }

            @Override
            public int getCount() {
                return data.getCount();
            }
        };
    }
    // Fusion Core end

    // @formatter:off
    @Shadow @Final private Container brewingStand;
    // @formatter:on

    private CraftBrewingStandView bukkitEntity = null;
    private Inventory playerInventory;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V", at = @At("RETURN"))
    public void arclight$init(int id, Inventory playerInventory, Container inventory, ContainerData p_i50096_4_, CallbackInfo ci) {
        this.playerInventory = playerInventory;
    }

    @Inject(method = "stillValid", cancellable = true, at = @At("HEAD"))
    public void arclight$unreachable(Player playerIn, CallbackInfoReturnable<Boolean> cir) {
        if (!bridge$isCheckReachable()) cir.setReturnValue(true);
    }

    @Override
    public CraftBrewingStandView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryBrewer inventory = new CraftInventoryBrewer(this.brewingStand);
        bukkitEntity = new CraftBrewingStandView(((PlayerBridge) this.playerInventory.player).bridge$getBukkitEntity(), inventory, (BrewingStandMenu) (Object) this);
        return bukkitEntity;
    }
}
