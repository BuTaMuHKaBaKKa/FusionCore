package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's clicked face of {@code BlockDamageEvent} (Paper patch 0864). Null when the event was built with Spigot's
 * constructor, as with Paper's deprecated one.
 */
@Mixin(value = BlockDamageEvent.class, remap = false)
public abstract class BlockDamageEventMixin {

    private BlockFace fusioncore$blockFace;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fusioncore$takeFace(CallbackInfo ci) {
        this.fusioncore$blockFace = FusionCoreEvents.takeConstructorArgument(BlockFace.class);
    }

    public BlockFace getBlockFace() {
        return this.fusioncore$blockFace;
    }
}
