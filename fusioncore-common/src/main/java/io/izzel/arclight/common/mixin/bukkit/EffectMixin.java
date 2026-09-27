package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.Effect;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Effect#isApplicable}; {@code STEP_SOUND} also takes {@code BlockData}. */
@Mixin(value = Effect.class, remap = false)
public abstract class EffectMixin {

    public boolean isApplicable(Object obj) {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.effectApplicable((Effect) (Object) this, obj);
    }
}
