package io.izzel.arclight.common.mixin.core.world.entity.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public AbstractVillager methods Paper's entity API calls. */
@Mixin(net.minecraft.world.entity.npc.AbstractVillager.class)
public interface AbstractVillagerApiInvoker {

    @Invoker("updateTrades")
    void fusioncore$updateTrades();
}
