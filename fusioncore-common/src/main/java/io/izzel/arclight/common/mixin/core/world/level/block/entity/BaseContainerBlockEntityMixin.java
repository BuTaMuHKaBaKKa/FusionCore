package io.izzel.arclight.common.mixin.core.world.level.block.entity;

import io.izzel.arclight.common.bridge.core.world.IInventoryBridge;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BaseContainerBlockEntity.class)
public abstract class BaseContainerBlockEntityMixin extends BlockEntityMixin implements IInventoryBridge, Container {

    @Override
    public Location getLocation() {
        return CraftBlock.at(this.level, this.worldPosition).getLocation();
    }

    @Override
    public RecipeHolder<?> getCurrentRecipe() {
        return null;
    }

    @Override
    public void setCurrentRecipe(RecipeHolder<?> recipe) {
    }

    // Fusion Core: Paper's BlockLockCheckEvent (patch 0778)
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "canOpen", at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/entity/BaseContainerBlockEntity;canUnlock(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/LockCode;Lnet/minecraft/network/chat/Component;)Z"))
    private boolean fusioncore$lockCheck(net.minecraft.world.entity.player.Player player, net.minecraft.world.LockCode lock, net.minecraft.network.chat.Component name,
                                       com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original) {
        Boolean result = io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.lockCheck(player, lock, name, (net.minecraft.world.level.block.entity.BlockEntity) (Object) this);
        return result != null ? result : original.call(player, lock, name);
    }
}
