package io.izzel.arclight.common.mod.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Fusion Core: Paper's static methods on {@link ItemStack} that Spigot lacks, reached through
 * {@code PaperCallTransformer} like {@link FusionCoreBukkit}. Bodies are Paper's.
 */
public final class FusionCoreItemStackStatics {

    private static final byte ARRAY_SERIALIZATION_VERSION = 1;

    private FusionCoreItemStackStatics() {
    }

    public static ItemStack of(Material type) {
        return of(type, 1);
    }

    public static ItemStack of(Material type, int amount) {
        com.google.common.base.Preconditions.checkArgument(type.asItemType() != null, "%s isn't an item", type);
        com.google.common.base.Preconditions.checkArgument(amount > 0, "amount must be greater than 0");
        return new ItemStack(type, amount);
    }

    public static ItemStack empty() {
        return new ItemStack(Material.AIR);
    }

    public static ItemStack deserializeBytes(byte[] bytes) {
        return FusionCoreItems.deserializeItem(bytes);
    }

    public static byte[] serializeItemsAsBytes(java.util.Collection<ItemStack> items) {
        try (final java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
            final java.io.DataOutput output = new java.io.DataOutputStream(outputStream);
            output.writeByte(ARRAY_SERIALIZATION_VERSION);
            output.writeInt(items.size());
            for (final ItemStack item : items) {
                if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                    output.writeInt(0);
                    continue;
                }
                final byte[] itemBytes = FusionCoreItems.serializeItem(item);
                output.writeInt(itemBytes.length);
                output.write(itemBytes);
            }
            return outputStream.toByteArray();
        } catch (final java.io.IOException e) {
            throw new RuntimeException("Error while writing itemstack", e);
        }
    }

    public static byte[] serializeItemsAsBytes(ItemStack[] items) {
        return serializeItemsAsBytes(java.util.Arrays.asList(items));
    }

    public static ItemStack[] deserializeItemsFromBytes(final byte[] bytes) {
        try (final java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(bytes)) {
            final java.io.DataInputStream input = new java.io.DataInputStream(inputStream);
            final byte version = input.readByte();
            if (version != ARRAY_SERIALIZATION_VERSION) {
                throw new IllegalArgumentException("Unsupported version or bad data: " + version);
            }
            final int count = input.readInt();
            final ItemStack[] items = new ItemStack[count];
            for (int i = 0; i < count; i++) {
                final int length = input.readInt();
                if (length == 0) {
                    items[i] = empty();
                    continue;
                }
                final byte[] itemBytes = new byte[length];
                input.readFully(itemBytes);
                items[i] = deserializeBytes(itemBytes);
            }
            return items;
        } catch (final java.io.IOException e) {
            throw new RuntimeException("Error while reading itemstack", e);
        }
    }
}
