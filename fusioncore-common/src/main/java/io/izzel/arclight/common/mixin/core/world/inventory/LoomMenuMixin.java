package io.izzel.arclight.common.mixin.core.world.inventory;

import io.izzel.arclight.common.bridge.core.world.entity.player.PlayerBridge;
import io.izzel.arclight.common.bridge.core.world.inventory.PosContainerBridge;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.LoomMenu;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryLoom;
import org.bukkit.craftbukkit.v.inventory.view.CraftLoomView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import io.izzel.arclight.common.mod.util.FusionCorePrepareResult;

@Mixin(LoomMenu.class)
public abstract class LoomMenuMixin extends AbstractContainerMenuMixin implements PosContainerBridge {

    // @formatter:off
    @Shadow @Final private Container inputContainer;
    @Shadow @Final private Container outputContainer;
    @Shadow @Final private ContainerLevelAccess access;
    @Shadow private java.util.List<net.minecraft.core.Holder<net.minecraft.world.level.block.entity.BannerPattern>> selectablePatterns;
    @Shadow @Final net.minecraft.world.inventory.DataSlot selectedBannerPatternIndex;
    @Shadow protected abstract void setupResultSlot(net.minecraft.core.Holder<net.minecraft.world.level.block.entity.BannerPattern> pattern);
    // @formatter:on

    private CraftLoomView bukkitEntity;
    private Inventory playerInventory;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    public void arclight$init(int id, Inventory playerInventory, ContainerLevelAccess worldCallable, CallbackInfo ci) {
        this.playerInventory = playerInventory;
    }

    // Fusion Core: Paper's PlayerLoomPatternSelectEvent (patch 0465); the event may pick a pattern the loom does not offer
    @Inject(method = "clickMenuButton", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V"))
    private void fusioncore$patternSelect(net.minecraft.world.entity.player.Player player, int id, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (!io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.listened(io.papermc.paper.event.player.PlayerLoomPatternSelectEvent.getHandlerList())
            || !(player instanceof PlayerBridge bridge)) {
            return;
        }
        var event = new io.papermc.paper.event.player.PlayerLoomPatternSelectEvent((org.bukkit.entity.Player) bridge.bridge$getBukkitEntity(),
            (org.bukkit.inventory.LoomInventory) getBukkitView().getTopInventory(),
            org.bukkit.craftbukkit.v.block.banner.CraftPatternType.minecraftHolderToBukkit(this.selectablePatterns.get(id)));
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.containerMenu.sendAllDataToRemote();
            cir.setReturnValue(false);
            return;
        }
        var eventPattern = org.bukkit.craftbukkit.v.block.banner.CraftPatternType.bukkitToMinecraftHolder(event.getPatternType());
        int index = -1;
        var selected = eventPattern;
        for (int i = 0; i < this.selectablePatterns.size(); i++) {
            if (eventPattern.equals(this.selectablePatterns.get(i))) {
                index = i;
                selected = this.selectablePatterns.get(i);
                break;
            }
        }
        player.containerMenu.sendAllDataToRemote();
        this.selectedBannerPatternIndex.set(index);
        this.setupResultSlot(selected);
        cir.setReturnValue(true);
    }

    @Inject(method = "stillValid", cancellable = true, at = @At("HEAD"))
    public void arclight$unreachable(Player playerIn, CallbackInfoReturnable<Boolean> cir) {
        if (!bridge$isCheckReachable()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public CraftLoomView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryLoom inventory = new CraftInventoryLoom(this.inputContainer, this.outputContainer);
        bukkitEntity = new CraftLoomView(((PlayerBridge) this.playerInventory.player).bridge$getBukkitEntity(), inventory, (LoomMenu) (Object) this);
        return bukkitEntity;
    }

    @Override
    public ContainerLevelAccess bridge$getWorldPos() {
        return this.access;
    }

    // Fusion Core: Paper's PrepareResultEvent, when a pattern result was computed
    @Inject(method = "slotsChanged", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/inventory/LoomMenu;broadcastChanges()V"))
    private void fusioncore$prepareResult(Container container, CallbackInfo ci) {
        FusionCorePrepareResult.call((LoomMenu) (Object) this, 3);
    }
}
