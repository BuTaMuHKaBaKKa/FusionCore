package io.izzel.arclight.common.mixin.bukkit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import io.izzel.arclight.common.mod.util.FusionCoreTypes;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's additions to {@link Material}; Arclight's own work on Material is in {@code MaterialMixin}. */
@Mixin(value = Material.class, remap = false)
public abstract class MaterialPaperMixin {

    private Material fusioncore$self() {
        return (Material) (Object) this;
    }

    /** Paper: air, cave air and void air. */
    public boolean isEmpty() {
        Material self = this.fusioncore$self();
        return self == Material.AIR || self == Material.CAVE_AIR || self == Material.VOID_AIR;
    }

    /** Paper patch 0590: the block has a collision shape; throws for non-blocks. */
    public boolean isCollidable() {
        return FusionCoreTypes.isCollidable(this.fusioncore$self());
    }

    @SuppressWarnings("removal")
    public io.papermc.paper.inventory.ItemRarity getItemRarity() {
        return FusionCoreTypes.paperItemRarity(this.fusioncore$self());
    }

    /** Paper: the item's default attributes for a slot, Spigot's {@code getDefaultAttributeModifiers(EquipmentSlot)}. */
    public Multimap<Attribute, AttributeModifier> getItemAttributes(EquipmentSlot equipmentSlot) {
        return this.fusioncore$self().getDefaultAttributeModifiers(equipmentSlot);
    }

    /** Paper patch 0539: the item's default attributes for every slot. */
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers() {
        org.bukkit.inventory.ItemType type = this.fusioncore$self().asItemType();
        Preconditions.checkArgument(type != null, "The Material is not an item!");
        return FusionCoreTypes.defaultAttributeModifiers(type);
    }
}
