package io.izzel.arclight.common.bridge.core.server.level;

/** Fusion Core: Paper's per-world state: void damage and the lightning rod search. */
public interface ServerLevelPaperBridge {

    boolean fusioncore$voidDamageEnabled();

    void fusioncore$setVoidDamageEnabled(boolean enabled);

    float fusioncore$voidDamageAmount();

    void fusioncore$setVoidDamageAmount(float amount);

    double fusioncore$voidDamageMinBuildHeightOffset();

    void fusioncore$setVoidDamageMinBuildHeightOffset(double offset);

    java.util.Optional<net.minecraft.core.BlockPos> fusioncore$findLightningRod(net.minecraft.core.BlockPos pos);
}
