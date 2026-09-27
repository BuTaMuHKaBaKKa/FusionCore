package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/** Fusion Core: reaches the protected table that backs {@link CommandMapMixin#getKnownCommands()}. */
@Mixin(value = SimpleCommandMap.class, remap = false)
public interface SimpleCommandMapAccessor {

    @Accessor("knownCommands")
    Map<String, Command> fusioncore$knownCommands();
}
