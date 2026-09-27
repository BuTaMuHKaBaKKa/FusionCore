package io.izzel.arclight.common.mixin.core.world.inventory;

import io.izzel.arclight.common.bridge.core.world.IInventoryBridge;
import io.izzel.arclight.common.bridge.core.world.inventory.PosContainerBridge;
import net.minecraft.world.inventory.CartographyTableMenu;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/inventory/CartographyTableMenu$2")
public abstract class CartographyContainer2Mixin implements IInventoryBridge {
    @Shadow(aliases = {"this$0", "f_39182_", "field_19273"}, remap = false)
    private CartographyTableMenu outerThis;

    @Override
    public Location getLocation() {
        return ((PosContainerBridge) outerThis).bridge$getWorldLocation();
    }

    // Fusion Core: as Paper (patch 0935, CartographyItemEvent), a change of the result slot does not recompute the result, so
    // plugins can put their own item there
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "setChanged", at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/CartographyTableMenu;slotsChanged(Lnet/minecraft/world/Container;)V"))
    private void fusioncore$keepResult(CartographyTableMenu menu, net.minecraft.world.Container container, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
    }
}