package io.izzel.arclight.common.bridge.bukkit;

/** Fusion Core: Paper's {@code EntityUnleashEvent} additions (dropLeash, cancelling; API patches 0241/0295). */
public interface EntityUnleashEventBridge {

    boolean bridge$isDropLeash();

    void bridge$setDropLeash(boolean dropLeash);

    boolean bridge$isCancelled();
}
