package io.izzel.arclight.common.mod.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockType;
import org.bukkit.craftbukkit.v.attribute.CraftAttribute;
import org.bukkit.craftbukkit.v.block.CraftBlockType;
import org.bukkit.craftbukkit.v.inventory.CraftItemType;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.Nullable;

/**
 * Fusion Core: bodies of Paper's additions to {@link Material}, {@link ItemType} and {@link BlockType}, called from
 * {@code MaterialPaperMixin}, {@code ItemTypeMixin} and {@code BlockTypeMixin}.
 */
public final class FusionCoreTypes {

    private FusionCoreTypes() {
    }

    /** Paper patch 0590: whether the block has a collision shape at all (vanilla's {@code hasCollision}). */
    public static boolean hasCollision(BlockType type) {
        return CraftBlockType.bukkitToMinecraftNew(type).hasCollision;
    }

    /** Paper's {@code Material#isCollidable}: {@link #hasCollision} of the block, which the material must be. */
    public static boolean isCollidable(Material material) {
        Preconditions.checkArgument(material.isBlock(), "%s isn't a block type", material);
        return CraftBlockType.bukkitToMinecraft(material).hasCollision;
    }

    /** Paper patch 1029: the item's default rarity component, or null if it has none. */
    public static org.bukkit.inventory.@Nullable ItemRarity itemRarity(ItemType type) {
        net.minecraft.world.item.Rarity rarity = CraftItemType.bukkitToMinecraftNew(type).components().get(DataComponents.RARITY);
        return rarity == null ? null : org.bukkit.inventory.ItemRarity.valueOf(rarity.name());
    }

    /**
     * Paper's deprecated {@code Material#getItemRarity}. Paper reads it from a new stack's meta, which throws for items
     * without an explicit rarity; here it is the item's default rarity (COMMON when it has none).
     */
    public static io.papermc.paper.inventory.ItemRarity paperItemRarity(Material material) {
        Preconditions.checkArgument(material.isItem(), "%s isn't an item", material);
        net.minecraft.world.item.Rarity rarity = CraftItemType.bukkitToMinecraft(material).components().get(DataComponents.RARITY);
        return io.papermc.paper.inventory.ItemRarity.valueOf(rarity == null ? "COMMON" : rarity.name());
    }

    /**
     * Paper patch 0539: the item's default attribute modifiers for every slot group. {@code body} (wolf and horse armour) is
     * Paper's {@code EquipmentSlotGroup.BODY}; a slot group Bukkit still does not know is reported as ANY.
     */
    public static Multimap<Attribute, AttributeModifier> defaultAttributeModifiers(ItemType type) {
        net.minecraft.world.item.Item item = CraftItemType.bukkitToMinecraftNew(type);
        ItemAttributeModifiers modifiers = item.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (modifiers.modifiers().isEmpty()) {
            modifiers = item.getDefaultAttributeModifiers();
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            org.bukkit.inventory.EquipmentSlotGroup group = org.bukkit.inventory.EquipmentSlotGroup.getByName(entry.slot().getSerializedName());
            net.minecraft.world.entity.ai.attributes.AttributeModifier nms = entry.modifier();
            builder.put(CraftAttribute.minecraftHolderToBukkit(entry.attribute()), new AttributeModifier(CraftNamespacedKey.fromMinecraft(nms.id()), nms.amount(),
                AttributeModifier.Operation.values()[nms.operation().ordinal()], group == null ? org.bukkit.inventory.EquipmentSlotGroup.ANY : group));
        }
        return builder.build();
    }
}
