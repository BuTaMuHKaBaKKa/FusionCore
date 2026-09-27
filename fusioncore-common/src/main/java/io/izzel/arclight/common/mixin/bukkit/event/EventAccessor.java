package io.izzel.arclight.common.mixin.bukkit.event;

import org.bukkit.event.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: sets the {@code async} flag of {@code PlayerEvent}, whose constructor Spigot keeps package-private. */
@Mixin(value = Event.class, remap = false)
public interface EventAccessor {

    @Mutable
    @Accessor("async")
    void fusioncore$setAsync(boolean async);
}
