package io.izzel.arclight.common.bridge.bukkit;

/** Fusion Core: the core reads Paper's flag of {@code LingeringPotionSplashEvent} (Paper patch 0425 "Expand LingeringPotion API"). */
public interface LingeringPotionSplashEventBridge {

    boolean allowsEmptyCreation();
}
