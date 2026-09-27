package io.izzel.arclight.common.mixin.core.world.level.gameevent;

import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Fusion Core: Paper's {@code RegistryEvents.GAME_EVENT} changes the radius of a registered game event in place (see
 * {@code PaperRegistryListenerManager#runBuiltInListeners}); the registry, holders and vanilla's constants keep the same object.
 */
@Mixin(GameEvent.class)
public interface GameEventPaperAccessor {

    @Mutable
    @Accessor("notificationRadius")
    void fusioncore$setNotificationRadius(int radius);
}
