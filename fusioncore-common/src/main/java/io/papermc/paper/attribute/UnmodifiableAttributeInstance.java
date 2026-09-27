package io.papermc.paper.attribute;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v.attribute.CraftAttributeInstance;

/** Fusion Core: Paper's server class from patch 0584 "Get entity default attributes", unchanged. */
public class UnmodifiableAttributeInstance extends CraftAttributeInstance {

    public UnmodifiableAttributeInstance(AttributeInstance handle, Attribute attribute) {
        super(handle, attribute);
    }

    @Override
    public void setBaseValue(double d) {
        throw new UnsupportedOperationException("Cannot modify default attributes");
    }

    @Override
    public void addModifier(AttributeModifier modifier) {
        throw new UnsupportedOperationException("Cannot modify default attributes");
    }

    @Override
    public void removeModifier(AttributeModifier modifier) {
        throw new UnsupportedOperationException("Cannot modify default attributes");
    }

    // Fusion Core: Paper's override from patch "Add UUID attribute modifier API" (no @Override: javac does not see the mixin's default)
    public void removeModifier(java.util.UUID uuid) {
        throw new UnsupportedOperationException("Cannot modify default attributes");
    }
}
