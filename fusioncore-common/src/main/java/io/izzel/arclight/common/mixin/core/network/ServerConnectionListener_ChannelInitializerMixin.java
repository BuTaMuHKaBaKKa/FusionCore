package io.izzel.arclight.common.mixin.core.network;

import io.netty.channel.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's channel initialization listeners (patch 0521): after the server set up a new connection's pipeline,
 * plugins registered in {@code ChannelInitializeListenerHolder} (ViaVersion and others) add their handlers.
 */
@Mixin(targets = "net.minecraft.server.network.ServerConnectionListener$1")
public class ServerConnectionListener_ChannelInitializerMixin {

    @Inject(method = "initChannel", at = @At("RETURN"))
    private void fusioncore$channelListeners(Channel channel, CallbackInfo ci) {
        io.papermc.paper.network.ChannelInitializeListenerHolder.callListeners(channel);
    }
}
