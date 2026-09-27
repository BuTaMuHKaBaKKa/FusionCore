package io.izzel.arclight.common.mixin.bukkit.plugin;

import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code RegisteredListener#getExecutor} (patch 0373, used by {@code /paper dumplisteners}). */
@Mixin(value = RegisteredListener.class, remap = false)
public abstract class RegisteredListenerMixin {

    @Shadow @Final private EventExecutor executor;

    public EventExecutor getExecutor() {
        return this.executor;
    }
}
