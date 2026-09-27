package io.izzel.arclight.common.mixin.core.server.level;

import com.google.common.collect.Lists;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.bridge.core.world.level.saveddata.maps.MapItemSavedDataBridge;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/level/ServerLevel$EntityCallbacks")
public class ServerLevel_EntityCallbacksMixin {
    @Shadow(aliases = {"f_143351_", "this$0", "field_26936"})
    private ServerLevel outerThis;

    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
    private void arclight$valid(Entity entity, CallbackInfo ci) {
        ((EntityBridge) entity).bridge$setInWorld(true);
        ((EntityBridge) entity).bridge$setValid(true);
        io.izzel.arclight.common.mod.util.FusionCoreEntities.initOrigin(entity, outerThis); // Fusion Core: Paper's Entity#getOrigin (patch 0036)
        io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents.addedToWorld(entity, outerThis); // Fusion Core: Paper's EntityAddToWorldEvent
    }

    @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    private void arclight$entityCleanup(Entity entity, CallbackInfo ci) {
        if (entity instanceof Player player) {
            for (ServerLevel serverLevel : ArclightServer.getMinecraftServer().getAllLevels()) {
                DimensionDataStorage worldData = serverLevel.getDataStorage();
                for (Object o : worldData.cache.values()) {
                    if (o instanceof MapItemSavedData map) {
                        map.carriedByPlayers.remove(player);
                        ((MapItemSavedDataBridge) map).bridge$getCarriedBy().removeIf(holdingPlayer -> holdingPlayer.player == entity);
                    }
                }
            }
        }
        if (((EntityBridge) entity).bridge$getBukkitEntity() instanceof InventoryHolder holder) {
            for (org.bukkit.entity.HumanEntity h : Lists.newArrayList(holder.getInventory().getViewers())) {
                io.izzel.arclight.common.mod.util.FusionCoreEvents.closeWith(org.bukkit.event.inventory.InventoryCloseEvent$Reason.UNLOADED, h::closeInventory); // Fusion Core: Paper's close reason
            }
        }
    }

    @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
    private void arclight$invalid(Entity entity, CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents.removedFromWorld(entity, outerThis); // Fusion Core: Paper's EntityRemoveFromWorldEvent, fired while valid
        ((EntityBridge) entity).bridge$setValid(false);
        if (!(entity instanceof ServerPlayer)) {
            for (var player : outerThis.players()) {
                ((ServerPlayerBridge) player).bridge$getBukkitEntity().onEntityRemove(entity);
            }
        }
    }
}