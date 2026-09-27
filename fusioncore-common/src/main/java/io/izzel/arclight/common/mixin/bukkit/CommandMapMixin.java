package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

/**
 * Fusion Core: Paper's {@code CommandMap#getKnownCommands()}. {@code CraftCommandMap} already implements it;
 * this body is only a fallback for other maps.
 */
@Mixin(value = CommandMap.class, remap = false)
public interface CommandMapMixin {

    default Map<String, Command> getKnownCommands() {
        return ((SimpleCommandMapAccessor) this).fusioncore$knownCommands();
    }
}
