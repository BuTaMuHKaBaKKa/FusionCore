package io.izzel.arclight.common.mixin.core.world.entity.player;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: player internals Paper's API reaches: skin parts ({@code ClientOption.SKIN_PARTS}, patch 0340), hurt direction (patch 0445). */
@Mixin(Player.class)
public interface PlayerPaperAccessor {

    @Accessor("DATA_PLAYER_MODE_CUSTOMISATION")
    static EntityDataAccessor<Byte> fusioncore$modeCustomisation() {
        throw new AssertionError();
    }

    @Accessor("hurtDir")
    void fusioncore$setHurtDir(float hurtDir);
}
