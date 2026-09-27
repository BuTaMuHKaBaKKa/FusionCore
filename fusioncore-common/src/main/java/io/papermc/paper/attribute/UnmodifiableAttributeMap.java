package io.papermc.paper.attribute;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v.attribute.CraftAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: Paper's server class from patch 0584 "Get entity default attributes"; the private
 * {@code AttributeSupplier#getAttributeInstance} is reached through an invoker. */
public class UnmodifiableAttributeMap implements Attributable {

    private final AttributeSupplier handle;

    public UnmodifiableAttributeMap(@NotNull AttributeSupplier handle) {
        this.handle = handle;
    }

    @Override
    public @Nullable AttributeInstance getAttribute(@NotNull Attribute attribute) {
        net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> nmsAttribute = CraftAttribute.bukkitToMinecraftHolder(attribute);
        if (!this.handle.hasAttribute(nmsAttribute)) {
            return null;
        }
        return new UnmodifiableAttributeInstance(((io.izzel.arclight.common.mixin.core.world.entity.api.AttributeSupplierApiInvoker) this.handle).fusioncore$getAttributeInstance(nmsAttribute), attribute);
    }

    // Fusion Core: Paper's Attributable#registerAttribute, not in Spigot's interface (Fusion Core adds it)
    public void registerAttribute(@NotNull Attribute attribute) {
        throw new UnsupportedOperationException("Cannot register new attributes here");
    }
}
