package io.izzel.arclight.common.mixin.core.server.network;

import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's PlayerChunkLoadEvent / PlayerChunkUnloadEvent (Paper patch 0443 "Player Chunk Load/Unload Events"):
 * right after a chunk is sent to a player, and after the client is told to forget one. Only built when a plugin listens.
 */
@Mixin(PlayerChunkSender.class)
public abstract class PlayerChunkSenderMixin {

    @Inject(method = "sendChunk", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private static void fusioncore$chunkLoad(ServerGamePacketListenerImpl handler, ServerLevel level, LevelChunk chunk, CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.state(handler.player).fusioncore$sentChunks().add(chunk.getPos().toLong()); // Fusion Core: Paper's sent chunks API (patch 1008)
        if (PlayerChunkLoadEvent.getHandlerList().getRegisteredListeners().length > 0) {
            Bukkit.getPluginManager().callEvent(new PlayerChunkLoadEvent(new CraftChunk(chunk), ((ServerPlayerBridge) handler.player).bridge$getBukkitEntity()));
        }
    }

    @Inject(method = "dropChunk", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void fusioncore$chunkUnload(ServerPlayer player, ChunkPos pos, CallbackInfo ci) {
        if (PlayerChunkUnloadEvent.getHandlerList().getRegisteredListeners().length > 0) {
            // as Paper: World#getChunkAt, which loads the chunk if it is already gone
            Bukkit.getPluginManager().callEvent(new PlayerChunkUnloadEvent(((WorldBridge) player.serverLevel()).bridge$getWorld().getChunkAt(pos.x, pos.z),
                ((ServerPlayerBridge) player).bridge$getBukkitEntity()));
        }
    }

    // Fusion Core: Paper's sent chunks API (Paper patch 1008): a dropped chunk is no longer sent (pending ones never were)
    @Inject(method = "dropChunk", at = @At("HEAD"))
    private void fusioncore$forgetSent(ServerPlayer player, ChunkPos pos, CallbackInfo ci) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.state(player).fusioncore$sentChunks().remove(pos.toLong());
    }
}
