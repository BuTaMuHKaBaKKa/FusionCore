package io.izzel.arclight.common.mixin.bukkit.command;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's component permission message on {@link Command}, stored as Spigot's legacy string. */
@Mixin(value = Command.class, remap = false)
@SuppressWarnings("removal")
public abstract class CommandMixin {

    /** Fusion Core: Paper's {@code Command#timings}; always the no-op timing. */
    public Timing timings = Timings.NULL_HANDLER;

    @Shadow public abstract String getName();

    @Shadow public abstract String getPermissionMessage();

    @Shadow public abstract Command setPermissionMessage(String permissionMessage);

    public Component permissionMessage() {
        return LegacyComponentSerializer.legacySection().deserializeOrNull(this.getPermissionMessage());
    }

    public void permissionMessage(Component permissionMessage) {
        this.setPermissionMessage(LegacyComponentSerializer.legacySection().serializeOrNull(permissionMessage));
    }

    /** Fusion Core: Paper's {@code Command#canBeOverriden} (patch 0465): only Paper's own Brigadier commands say true. */
    public boolean canBeOverriden() {
        return false;
    }

    /** Fusion Core: Paper's {@code Command#getTimingName} (patch 0011): the name timings group the command under. */
    public String getTimingName() {
        return this.getName();
    }
}
