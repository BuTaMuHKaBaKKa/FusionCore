package io.izzel.arclight.common.bridge.bukkit;

import net.kyori.adventure.text.Component;

import java.util.List;

public interface ItemMetaBridge {

    /** Fusion Core: Paper's {@code ItemMeta#lore()}, callable from core code (javac does not see the mixin-added one). */
    List<Component> fusioncore$lore();

    void fusioncore$lore(List<? extends Component> lore);
}
