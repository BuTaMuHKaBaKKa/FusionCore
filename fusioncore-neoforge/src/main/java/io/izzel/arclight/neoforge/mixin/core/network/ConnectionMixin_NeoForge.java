package io.izzel.arclight.neoforge.mixin.core.network;

import net.minecraft.network.Connection;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(Connection.class)
public class ConnectionMixin_NeoForge {

    /*
     * Fusion Core: "Connection reset" and similar client-side disconnects are logged at DEBUG; other errors stay at ERROR.
     */
    @Redirect(method = "exceptionCaught", at = @At(value = "INVOKE", remap = false, target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V"))
    private void fusioncore$quietConnectionReset(Logger logger, String message, Throwable throwable) {
        if (throwable instanceof IOException) {
            logger.debug(message, throwable);
        } else {
            logger.error(message, throwable);
        }
    }
}
