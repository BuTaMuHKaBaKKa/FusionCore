package io.izzel.arclight.common.mixin.core.world.level.block;

import io.izzel.arclight.common.bridge.core.world.level.portal.DimensionTransitionBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.portal.DimensionTransition;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndGatewayBlock.class)
public class EndGatewayBlockMixin {

    // Fusion Core: Paper fires EntityPortalEnterEvent for end gateways too, cancellable (patch 0781)
    @Inject(method = "entityInside", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private void fusioncore$portalEnter(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, BlockPos pos, Entity entity,
                                      org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (level.isClientSide) {
            return;
        }
        var event = io.izzel.arclight.common.mod.util.FusionCoreEvents.newEntityPortalEnterEvent(entity.bridge$getBukkitEntity(),
            new org.bukkit.Location(level.bridge$getWorld(), pos.getX(), pos.getY(), pos.getZ()), io.izzel.arclight.common.mod.util.FusionCoreEnumConstants.PortalType$END_GATEWAY());
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (((org.bukkit.event.Cancellable) (Object) event).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getPortalDestination", at = @At("RETURN"))
    private void arclight$setCause(ServerLevel serverLevel, Entity entity, BlockPos blockPos, CallbackInfoReturnable<DimensionTransition> cir) {
        var dimensionTransition = cir.getReturnValue();
        if (dimensionTransition != null) {
            ((DimensionTransitionBridge) ((Object) dimensionTransition)).bridge$setTeleportCause(PlayerTeleportEvent.TeleportCause.END_GATEWAY);
        }
    }
}
