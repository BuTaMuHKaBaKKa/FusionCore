package io.izzel.arclight.common.bridge.bukkit;

import net.kyori.adventure.text.Component;

import java.util.List;

/** Fusion Core: Paper's component book methods, callable from core code (see FusionCoreBookBuilder). */
public interface BookMetaBridge {

    void fusioncore$addPages(List<Component> pages);

    /** Written books only; a writable book has neither. */
    default void fusioncore$setTitleAuthor(Component title, Component author) {
    }
}
