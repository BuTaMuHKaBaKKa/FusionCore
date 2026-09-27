package io.izzel.arclight.common.mixin.core.network.protocol.game;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: the id of an entity the server does not know, for Paper's PlayerUseUnknownEntityEvent. */
@Mixin(ServerboundInteractPacket.class)
public interface ServerboundInteractPacketAccessor {

    @Accessor("entityId")
    int fusioncore$getEntityId();
}
