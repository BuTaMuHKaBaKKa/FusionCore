package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CraftHumanEntity.class)
public abstract class CraftHumanEntityMixin extends CraftEntity {

    // Fusion Core: Paper's InventoryOpenEvent#titleOverride (patch 0874) for inventories plugins open
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "openCustomInventory",
        at = @org.spongepowered.asm.mixin.injection.At(value = "NEW", target = "net/minecraft/network/protocol/game/ClientboundOpenScreenPacket"))
    private static net.minecraft.network.protocol.game.ClientboundOpenScreenPacket fusioncore$titleOverride(int id, net.minecraft.world.inventory.MenuType<?> type,
            net.minecraft.network.chat.Component title, com.llamalad7.mixinextras.injector.wrapoperation.Operation<net.minecraft.network.protocol.game.ClientboundOpenScreenPacket> original,
            @com.llamalad7.mixinextras.sugar.Local net.minecraft.world.inventory.AbstractContainerMenu container) {
        return original.call(id, type, fusioncore$title(container, title));
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "openInventory(Lorg/bukkit/inventory/InventoryView;)V",
        at = @org.spongepowered.asm.mixin.injection.At(value = "NEW", target = "net/minecraft/network/protocol/game/ClientboundOpenScreenPacket"))
    private net.minecraft.network.protocol.game.ClientboundOpenScreenPacket fusioncore$titleOverrideView(int id, net.minecraft.world.inventory.MenuType<?> type,
            net.minecraft.network.chat.Component title, com.llamalad7.mixinextras.injector.wrapoperation.Operation<net.minecraft.network.protocol.game.ClientboundOpenScreenPacket> original,
            @com.llamalad7.mixinextras.sugar.Local net.minecraft.world.inventory.AbstractContainerMenu container) {
        return original.call(id, type, fusioncore$title(container, title));
    }

    private static net.minecraft.network.chat.Component fusioncore$title(net.minecraft.world.inventory.AbstractContainerMenu container, net.minecraft.network.chat.Component title) {
        net.minecraft.network.chat.Component override = container == null ? null
            : ((io.izzel.arclight.common.bridge.core.world.inventory.AbstractContainerMenuBridge) container).fusioncore$getTitleOverride();
        return override != null ? override : title;
    }

    // @formatter:off
    @Shadow(remap = false) public abstract Player getHandle();
    @Shadow(remap = false) public abstract void setHandle(Player entity);
    // @formatter:on

    public CraftHumanEntityMixin(CraftServer server, Entity entity) {
        super(server, entity);
    }

    @Decorate(method = "getOpenInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;getBukkitView()Lorg/bukkit/inventory/InventoryView;"))
    private InventoryView arclight$capturePlayer(AbstractContainerMenu instance) throws Throwable {
        Player handle = getHandle();
        try {
            ArclightCaptures.captureContainerOwner(handle);
            return (InventoryView) DecorationOps.callsite().invoke(instance);
        } finally {
            ArclightCaptures.popContainerOwner(handle);
        }
    }

    @Override
    public void setHandle(Entity entity) {
        setHandle((Player) entity);
    }

    // Fusion Core: Paper's close reasons (Paper patch 0208) for Bukkit's own closes

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "closeInventory()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;closeContainer()V"))
    private void fusioncore$closeByPlugin(Player handle, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        io.izzel.arclight.common.mod.util.FusionCoreEvents.closeWith(org.bukkit.event.inventory.InventoryCloseEvent$Reason.PLUGIN, () -> original.call(handle));
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "openInventory(Lorg/bukkit/inventory/InventoryView;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;handleContainerClose(Lnet/minecraft/network/protocol/game/ServerboundContainerClosePacket;)V"))
    private void fusioncore$closeForNew(net.minecraft.server.network.ServerGamePacketListenerImpl connection, net.minecraft.network.protocol.game.ServerboundContainerClosePacket packet, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        io.izzel.arclight.common.mod.util.FusionCoreEvents.closeWith(org.bukkit.event.inventory.InventoryCloseEvent$Reason.OPEN_NEW, () -> original.call(connection, packet));
    }
}
