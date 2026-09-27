package io.izzel.arclight.common.bridge.core.world.entity.api;

import net.kyori.adventure.util.TriState;

/** Fusion Core: Paper's friction state of living entities and items (patch 0769), saved as {@code Paper.FrictionState}. */
public interface FrictionApiBridge {

    TriState fusioncore$frictionState();

    void fusioncore$setFrictionState(TriState state);

    static void save(TriState state, net.minecraft.nbt.CompoundTag tag) {
        if (state != TriState.NOT_SET) {
            tag.putString("Paper.FrictionState", state.toString());
        }
    }

    static TriState read(TriState current, net.minecraft.nbt.CompoundTag tag, Object owner) {
        if (!tag.contains("Paper.FrictionState")) {
            return current;
        }
        String fs = tag.getString("Paper.FrictionState");
        try {
            return TriState.valueOf(fs);
        } catch (Exception ignored) {
            com.mojang.logging.LogUtils.getLogger().error("Unknown friction state " + fs + " for " + owner);
            return current;
        }
    }
}
