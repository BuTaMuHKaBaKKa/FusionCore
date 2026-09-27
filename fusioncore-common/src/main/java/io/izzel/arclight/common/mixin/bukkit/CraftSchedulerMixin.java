package io.izzel.arclight.common.mixin.bukkit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.craftbukkit.v.scheduler.CraftScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ThreadFactory;

@Mixin(value = CraftScheduler.class, remap = false)
public class CraftSchedulerMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/util/concurrent/ThreadFactoryBuilder;build()Ljava/util/concurrent/ThreadFactory;"))
    private ThreadFactory arclight$setDaemon(ThreadFactoryBuilder instance) {
        return instance.setDaemon(true).build();
    }

    /** Fusion Core: Paper's ServerExceptionEvent (Paper patch 0062) for a synchronous task that threw. */
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "mainThreadHeartbeat", at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;log(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V"))
    private void fusioncore$taskException(java.util.logging.Logger logger, java.util.logging.Level level, String message, Throwable thrown, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
                                        @com.llamalad7.mixinextras.sugar.Local(ordinal = 0) org.bukkit.craftbukkit.v.scheduler.CraftTask task) {
        original.call(logger, level, message, thrown);
        org.bukkit.Bukkit.getPluginManager().callEvent(new com.destroystokyo.paper.event.server.ServerExceptionEvent(
            new com.destroystokyo.paper.exception.ServerSchedulerException(message, thrown, task)));
    }
}
