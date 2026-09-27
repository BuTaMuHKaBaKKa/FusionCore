package io.izzel.arclight.common.mixin.core.world.entity.raid;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: {@code Raid#getBossBar} (Paper patch 0936). */
@Mixin(Raid.class)
public interface RaidPaperAccessor {

    @Accessor("raidEvent")
    ServerBossEvent fusioncore$raidEvent();
}
