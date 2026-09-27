package io.izzel.arclight.common.mixin.core.world.inventory;

import io.izzel.arclight.common.bridge.core.world.entity.player.PlayerBridge;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.bukkit.craftbukkit.v.inventory.CraftInventory;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryBeacon;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v.inventory.view.CraftBeaconView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeaconMenu.class)
public abstract class BeaconMenuMixin extends AbstractContainerMenuMixin {

    // @formatter:off
    @Shadow @Final private Container beacon;
    // @formatter:on

    private CraftBeaconView bukkitEntity;
    private Inventory playerInventory;

    @Inject(method = "<init>(ILnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    public void arclight$init(int id, Container inventory, ContainerData p_i50100_3_, ContainerLevelAccess worldPosCallable, CallbackInfo ci) {
        this.playerInventory = (Inventory) inventory;
    }

    @Inject(method = "stillValid", cancellable = true, at = @At("HEAD"))
    public void arclight$unreachable(Player playerIn, CallbackInfoReturnable<Boolean> cir) {
        if (!bridge$isCheckReachable()) cir.setReturnValue(true);
    }

    @Override
    public CraftBeaconView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        var inventory = new CraftInventoryBeacon(this.beacon);
        bukkitEntity = new CraftBeaconView(((PlayerBridge) this.playerInventory.player).bridge$getBukkitEntity(), inventory, (BeaconMenu) (Object) this);
        return bukkitEntity;
    }

    // Fusion Core: Paper's PlayerChangeBeaconEffectEvent (Paper patch 0478): the listener may change both effects, keep the
    // payment item, or cancel the change
    @org.spongepowered.asm.mixin.Shadow @Final private net.minecraft.world.inventory.ContainerData beaconData;
    @org.spongepowered.asm.mixin.Shadow @Final private net.minecraft.world.inventory.ContainerLevelAccess access;

    @Inject(method = "updateEffects", cancellable = true, at = @At("HEAD"))
    private void fusioncore$changeEffects(java.util.Optional<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>> primary,
                                        java.util.Optional<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>> secondary, CallbackInfo ci) {
        net.minecraft.world.inventory.Slot paymentSlot = ((net.minecraft.world.inventory.AbstractContainerMenu) (Object) this).getSlot(0); // vanilla's payment slot
        if (!paymentSlot.hasItem() || io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return; // vanilla
        }
        ci.cancel();
        org.bukkit.block.Block block = this.access.evaluate((level, pos) -> (org.bukkit.block.Block) org.bukkit.craftbukkit.v.block.CraftBlock.at(level, pos)).orElse(null);
        io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent event = new io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent(
            (org.bukkit.entity.Player) ((PlayerBridge) this.playerInventory.player).bridge$getBukkitEntity(),
            primary.map(org.bukkit.craftbukkit.v.potion.CraftPotionEffectType::minecraftHolderToBukkit).orElse(null),
            secondary.map(org.bukkit.craftbukkit.v.potion.CraftPotionEffectType::minecraftHolderToBukkit).orElse(null), block);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        this.beaconData.set(1, net.minecraft.world.inventory.BeaconMenu.encodeEffect(event.getPrimary() == null ? null : org.bukkit.craftbukkit.v.potion.CraftPotionEffectType.bukkitToMinecraftHolder(event.getPrimary())));
        this.beaconData.set(2, net.minecraft.world.inventory.BeaconMenu.encodeEffect(event.getSecondary() == null ? null : org.bukkit.craftbukkit.v.potion.CraftPotionEffectType.bukkitToMinecraftHolder(event.getSecondary())));
        if (event.willConsumeItem()) {
            paymentSlot.remove(1);
        }
        this.access.execute(net.minecraft.world.level.Level::blockEntityChanged);
    }
}
