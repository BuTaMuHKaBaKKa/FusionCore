package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreSigns;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/**
 * Fusion Core: Paper's component lines on {@link Sign}, the front side as in Paper (Paper patch 0006 "Adventure"), and the
 * allowed editor and interactable side (patch 0833 "More Sign Block API").
 */
@Mixin(value = Sign.class, remap = false)
public interface SignMixin {

    default List<Component> lines() {
        return FusionCoreSigns.lines(((Sign) this).getSide(Side.FRONT));
    }

    default Component line(int index) {
        return FusionCoreSigns.line(((Sign) this).getSide(Side.FRONT), index);
    }

    default void line(int index, Component line) {
        FusionCoreSigns.line(((Sign) this).getSide(Side.FRONT), index, line);
    }

    default java.util.UUID getAllowedEditorUniqueId() {
        return io.izzel.arclight.common.mod.util.FusionCoreBlockStates.allowedEditor(this);
    }

    default void setAllowedEditorUniqueId(java.util.UUID uuid) {
        io.izzel.arclight.common.mod.util.FusionCoreBlockStates.setAllowedEditor(this, uuid);
    }

    default Side getInteractableSideFor(org.bukkit.entity.Entity entity) {
        org.bukkit.Location location = entity.getLocation();
        return this.getInteractableSideFor(location.getX(), location.getZ());
    }

    default Side getInteractableSideFor(io.papermc.paper.math.Position position) {
        return this.getInteractableSideFor(position.x(), position.z());
    }

    default Side getInteractableSideFor(double x, double z) {
        return io.izzel.arclight.common.mod.util.FusionCoreBlockStates.interactableSide(this, x, z);
    }
}
