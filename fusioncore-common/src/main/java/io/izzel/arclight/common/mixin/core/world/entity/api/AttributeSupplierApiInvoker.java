package io.izzel.arclight.common.mixin.core.world.entity.api;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: the private AttributeSupplier method Paper's default entity attributes read (patch 0584). */
@Mixin(AttributeSupplier.class)
public interface AttributeSupplierApiInvoker {

    @Invoker("getAttributeInstance")
    AttributeInstance fusioncore$getAttributeInstance(Holder<Attribute> attribute);
}
