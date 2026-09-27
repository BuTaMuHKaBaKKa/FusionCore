package io.izzel.arclight.common.mixin.core.world.inventory;

import io.izzel.arclight.common.bridge.core.world.entity.player.PlayerBridge;
import io.izzel.arclight.common.bridge.core.world.inventory.PosContainerBridge;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.StonecutterMenu;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryStonecutter;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v.inventory.view.CraftStonecutterView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.izzel.arclight.common.mod.util.FusionCorePrepareResult;

@Mixin(StonecutterMenu.class)
public abstract class StonecutterMenuMixin extends AbstractContainerMenuMixin implements PosContainerBridge {

    // @formatter:off
    @Shadow @Final public Container container;
    @Shadow @Final private ResultContainer resultContainer;
    @Shadow @Final private ContainerLevelAccess access;
    // @formatter:on

    private CraftStonecutterView bukkitEntity = null;
    private Inventory playerInventory;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    public void arclight$init(int windowIdIn, Inventory playerInventoryIn, ContainerLevelAccess worldPosCallableIn, CallbackInfo ci) {
        this.playerInventory = playerInventoryIn;
    }

    @Override
    public CraftStonecutterView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryStonecutter inventory = new CraftInventoryStonecutter(this.container, this.resultContainer);
        bukkitEntity = new CraftStonecutterView(((PlayerBridge) this.playerInventory.player).bridge$getBukkitEntity(), inventory, (StonecutterMenu) (Object) this);
        return bukkitEntity;
    }

    @Override
    public ContainerLevelAccess bridge$getWorldPos() {
        return this.access;
    }

    // Fusion Core: Paper's PrepareResultEvent
    @Inject(method = "slotsChanged", at = @At("RETURN"))
    private void fusioncore$prepareResult(Container container, CallbackInfo ci) {
        FusionCorePrepareResult.call((StonecutterMenu) (Object) this, StonecutterMenu.RESULT_SLOT);
    }

    // Fusion Core: Paper's PlayerStonecutterRecipeSelectEvent (Paper patch 0480): cancelled keeps the old selection, a
    // listener may pick another recipe of the list
    @Shadow @Final private net.minecraft.world.inventory.DataSlot selectedRecipeIndex;
    @Shadow abstract void setupResultSlot();

    @Inject(method = "clickMenuButton", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V"))
    private void fusioncore$recipeSelectEvent(net.minecraft.world.entity.player.Player player, int id, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer)
            || io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }
        StonecutterMenu self = (StonecutterMenu) (Object) this;
        io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent event = new io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent(
            (org.bukkit.entity.Player) ((PlayerBridge) player).bridge$getBukkitEntity(), this.getBukkitView().getTopInventory(),
            (org.bukkit.inventory.StonecuttingRecipe) ((io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeHolderBridge) (Object) self.getRecipes().get(id)).bridge$toBukkitRecipe());
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.containerMenu.sendAllDataToRemote();
            cir.setReturnValue(false);
            return;
        }
        net.minecraft.resources.ResourceLocation key = org.bukkit.craftbukkit.v.util.CraftNamespacedKey.toMinecraft(event.getStonecuttingRecipe().getKey());
        if (!self.getRecipes().get(id).id().equals(key)) {
            for (int index = 0; index < self.getRecipes().size(); index++) {
                if (self.getRecipes().get(index).id().equals(key)) {
                    this.selectedRecipeIndex.set(index);
                    this.setupResultSlot();
                    player.containerMenu.sendAllDataToRemote();
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }
}
