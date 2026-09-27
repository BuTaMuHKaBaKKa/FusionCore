package io.izzel.arclight.common.bridge.core.world.entity.api;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: Paper's changeable ender dragon podium (patch 0687); not saved, as in Paper. */
public interface EnderDragonApiBridge {

    /** The podium plugins set, or vanilla's podium at the fight origin. */
    BlockPos fusioncore$getPodium();

    @Nullable BlockPos fusioncore$customPodium();

    void fusioncore$setPodium(@Nullable BlockPos podium);
}
