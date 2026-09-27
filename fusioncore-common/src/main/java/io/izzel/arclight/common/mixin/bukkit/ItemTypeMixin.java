package io.izzel.arclight.common.mixin.bukkit;

import com.google.common.collect.Multimap;
import io.izzel.arclight.common.mod.util.FusionCoreTypes;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemType;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@link ItemType} additions (patches 0539, 1029); {@code FeatureDependant} (patch "Add FeatureFlag API"). */
@Mixin(value = ItemType.class, remap = false)
public interface ItemTypeMixin extends io.papermc.paper.world.flag.FeatureDependant {

    default Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers() {
        return FusionCoreTypes.defaultAttributeModifiers((ItemType) this);
    }

    default org.bukkit.inventory.ItemRarity getItemRarity() {
        return FusionCoreTypes.itemRarity((ItemType) this);
    }
}
