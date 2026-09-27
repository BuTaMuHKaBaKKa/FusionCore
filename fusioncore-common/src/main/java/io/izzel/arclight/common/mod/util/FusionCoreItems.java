package io.izzel.arclight.common.mod.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.izzel.arclight.common.bridge.bukkit.ItemMetaBridge;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.GsonDataComponentValue;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Fusion Core: bodies of Paper's item text API ({@code CraftItemFactory#displayName}/{@code asHoverEvent}
 * from Paper patch 0010 "Adventure"), shared by the {@code ItemStack} and {@code ItemFactory} mixins.
 */
public final class FusionCoreItems {

    private FusionCoreItems() {
    }

    /** Paper: the stack's formatted name, as vanilla shows it in chat (brackets, rarity colour, hover). */
    public static Component displayName(ItemStack itemStack) {
        return FusionCoreAdventure.asAdventure(CraftItemStack.asNMSCopy(itemStack).getDisplayName());
    }

    /** Paper: a {@code show_item} hover with the item's id, count and component patch. */
    public static HoverEvent<HoverEvent.ShowItem> asHoverEvent(ItemStack item, UnaryOperator<HoverEvent.ShowItem> op) {
        DataComponentPatch patch = CraftItemStack.asNMSCopy(item).getComponentsPatch();
        return HoverEvent.showItem(op.apply(HoverEvent.ShowItem.showItem(
            Key.key(item.getType().getKey().toString()), item.getAmount(), asAdventure(patch))));
    }

    /** Paper's {@code PaperAdventure#asAdventure(DataComponentPatch)}, through JSON as the rest of Fusion Core's Adventure bridge. */
    private static Map<Key, DataComponentValue> asAdventure(DataComponentPatch patch) {
        if (patch.isEmpty()) {
            return Map.of();
        }
        RegistryOps<JsonElement> ops = ArclightServer.getMinecraftServer().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        Map<Key, DataComponentValue> out = new LinkedHashMap<>();
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
            DataComponentType<?> type = entry.getKey();
            ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
            if (id == null || type.isTransient()) {
                continue;
            }
            Key key = Key.key(id.getNamespace(), id.getPath());
            if (entry.getValue().isEmpty()) {
                out.put(key, DataComponentValue.removed());
            } else {
                encode(type, entry.getValue().get(), ops).ifPresent(json -> out.put(key, GsonDataComponentValue.gsonDataComponentValue(json)));
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<JsonElement> encode(DataComponentType<T> type, Object value, RegistryOps<JsonElement> ops) {
        return type.codecOrThrow().encodeStart(ops, (T) value).result();
    }

    public static List<Component> lore(ItemMeta meta) {
        return ((ItemMetaBridge) meta).fusioncore$lore();
    }

    public static void lore(ItemMeta meta, List<? extends Component> lore) {
        ((ItemMetaBridge) meta).fusioncore$lore(lore);
    }

    private static net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> holder(org.bukkit.enchantments.Enchantment enchantment) {
        net.minecraft.world.item.enchantment.Enchantment handle = ((org.bukkit.craftbukkit.v.enchantments.CraftEnchantment) enchantment).getHandle();
        return ArclightServer.getMinecraftServer().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).wrapAsHolder(handle);
    }

    /** Paper's {@code CraftEnchantment#displayName(int)}: vanilla's full name (level numeral, curses red). */
    public static Component enchantmentName(org.bukkit.enchantments.Enchantment enchantment, int level) {
        return FusionCoreAdventure.asAdventure(net.minecraft.world.item.enchantment.Enchantment.getFullname(holder(enchantment), level));
    }

    /** Paper's {@code CraftEnchantment#description()}. */
    public static Component enchantmentDescription(org.bukkit.enchantments.Enchantment enchantment) {
        return FusionCoreAdventure.asAdventure(((org.bukkit.craftbukkit.v.enchantments.CraftEnchantment) enchantment).getHandle().description());
    }

    /** Paper's {@code CraftMagicNumbers#serializeItem}: the item's NBT with "DataVersion", gzip-compressed. */
    public static byte[] serializeItem(ItemStack item) {
        com.google.common.base.Preconditions.checkNotNull(item, "null cannot be serialized");
        com.google.common.base.Preconditions.checkArgument(item.getType() != org.bukkit.Material.AIR, "air cannot be serialized");
        net.minecraft.nbt.CompoundTag compound = (net.minecraft.nbt.CompoundTag) CraftItemStack.asNMSCopy(item).save(ArclightServer.getMinecraftServer().registryAccess());
        compound.putInt("DataVersion", net.minecraft.SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        try {
            net.minecraft.nbt.NbtIo.writeCompressed(compound, out);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    /** Paper's {@code CraftMagicNumbers#deserializeItem}: reads, updates older data with vanilla's DataFixer, parses. */
    public static ItemStack deserializeItem(byte[] data) {
        com.google.common.base.Preconditions.checkNotNull(data, "null cannot be deserialized");
        com.google.common.base.Preconditions.checkArgument(data.length > 0, "cannot deserialize nothing");
        net.minecraft.nbt.CompoundTag compound;
        try {
            compound = net.minecraft.nbt.NbtIo.readCompressed(new java.io.ByteArrayInputStream(data), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        int current = net.minecraft.SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        int dataVersion = compound.getInt("DataVersion");
        com.google.common.base.Preconditions.checkArgument(dataVersion <= current, "Newer version! Server downgrades are not supported!");
        if (dataVersion < current) {
            compound = (net.minecraft.nbt.CompoundTag) net.minecraft.util.datafix.DataFixers.getDataFixer().update(net.minecraft.util.datafix.fixes.References.ITEM_STACK,
                new com.mojang.serialization.Dynamic<>(net.minecraft.nbt.NbtOps.INSTANCE, compound), dataVersion, current).getValue();
        }
        return CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.parse(ArclightServer.getMinecraftServer().registryAccess(), compound).orElseThrow());
    }

    /** Paper's {@code ItemStack#getPersistentDataContainer()}: the meta's container, empty for air. */
    public static io.papermc.paper.persistence.PersistentDataContainerView persistentData(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = org.bukkit.Bukkit.getItemFactory().getItemMeta(org.bukkit.Material.STONE);
        }
        return (io.papermc.paper.persistence.PersistentDataContainerView) (Object) meta.getPersistentDataContainer();
    }

    /** Paper's {@code CraftPersistentDataContainer#serializeToBytes}: the container's compound, uncompressed. */
    public static byte[] serializeContainer(org.bukkit.persistence.PersistentDataContainer container) throws java.io.IOException {
        net.minecraft.nbt.CompoundTag root = ((org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer) container).toTagCompound();
        java.io.ByteArrayOutputStream byteArrayOutput = new java.io.ByteArrayOutputStream();
        try (java.io.DataOutputStream dataOutput = new java.io.DataOutputStream(byteArrayOutput)) {
            net.minecraft.nbt.NbtIo.write(root, dataOutput);
            return byteArrayOutput.toByteArray();
        }
    }

    /** Paper's {@code CraftPersistentDataContainer#readFromBytes}. */
    public static void readContainer(org.bukkit.persistence.PersistentDataContainer container, byte[] bytes, boolean clear) throws java.io.IOException {
        org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer craft = (org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer) container;
        if (clear) {
            craft.getRaw().clear();
        }
        try (java.io.DataInputStream dataInput = new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes))) {
            net.minecraft.nbt.CompoundTag compound = net.minecraft.nbt.NbtIo.read(dataInput);
            craft.putAll(compound);
        }
    }

    // Fusion Core: Paper's ItemStack/ItemFactory/UnsafeValues item helpers (Paper patches 0143 ensureServerConversions,
    // 0144 getI18NDisplayName, 0193 getMaxItemUseDuration, 0532 repair check, 0679 enchantWithLevels, 0927 tooltip,
    // 0934 withType, 1054 JSON serialization). Paper's plain ItemStack delegates these to a CraftItemStack; here a
    // plain stack is copied to a vanilla stack instead, which gives the same result.

    private static net.minecraft.world.item.ItemStack handleOrCopy(ItemStack item) {
        if (item instanceof CraftItemStack craft) {
            net.minecraft.world.item.ItemStack handle = ((io.izzel.arclight.common.mixin.bukkit.CraftItemStackAccessor) (Object) craft).fusioncore$handle();
            return handle == null ? net.minecraft.world.item.ItemStack.EMPTY : handle;
        }
        return CraftItemStack.asNMSCopy(item);
    }

    /** Paper's {@code CraftItemFactory#ensureServerConversions}: the stack as the server would store it. */
    public static ItemStack ensureServerConversions(ItemStack item) {
        return CraftItemStack.asCraftMirror(CraftItemStack.asNMSCopy(item));
    }

    /** Paper's {@code CraftItemFactory#getI18NDisplayName}: the item's default name in the server's language (en_us). */
    public static String i18nDisplayName(ItemStack item) {
        net.minecraft.world.item.ItemStack nms = handleOrCopy(item);
        return net.minecraft.locale.Language.getInstance().getOrDefault(nms.getItem().getDescriptionId(nms));
    }

    /** Paper's {@code CraftItemStack#getMaxItemUseDuration}: how long the item can be used (eaten, drawn...). */
    public static int maxItemUseDuration(ItemStack item, org.bukkit.entity.LivingEntity entity) {
        net.minecraft.world.item.ItemStack handle = handleOrCopy(item);
        if (handle.isEmpty()) {
            return 0;
        }
        // Make sure plugins calling the old method don't blow up
        if (entity == null && handle.is(net.minecraft.world.item.Items.CROSSBOW)) {
            throw new UnsupportedOperationException("This item requires an entity to determine the max use duration");
        }
        return handle.getUseDuration(entity != null ? ((org.bukkit.craftbukkit.v.entity.CraftLivingEntity) entity).getHandle() : null);
    }

    /** Paper's {@code CraftMagicNumbers#isValidRepairItemStack}: whether {@code repairMaterial} repairs the item in an anvil. */
    public static boolean isValidRepairItemStack(ItemStack itemToBeRepaired, ItemStack repairMaterial) {
        if (!itemToBeRepaired.getType().isItem() || !repairMaterial.getType().isItem()) {
            return false;
        }
        return org.bukkit.craftbukkit.v.inventory.CraftItemType.bukkitToMinecraft(itemToBeRepaired.getType())
            .isValidRepairItem(CraftItemStack.asNMSCopy(itemToBeRepaired), CraftItemStack.asNMSCopy(repairMaterial));
    }

    /** Paper's {@code CraftItemStack#withType}: a copy of the stack with another item type and the same components. */
    public static ItemStack withType(ItemStack item, org.bukkit.Material type) {
        if (type == org.bukkit.Material.AIR) {
            return CraftItemStack.asCraftMirror(null);
        }
        final net.minecraft.world.item.ItemStack copy = new net.minecraft.world.item.ItemStack(
            org.bukkit.craftbukkit.v.inventory.CraftItemType.bukkitToMinecraft(type), item.getAmount());
        net.minecraft.world.item.ItemStack handle = handleOrCopy(item);
        if (!handle.isEmpty()) {
            copy.applyComponents(handle.getComponentsPatch());
        }
        final CraftItemStack mirrored = CraftItemStack.asCraftMirror(copy);
        mirrored.setItemMeta(mirrored.getItemMeta());
        return mirrored;
    }

    /** Paper's {@code CraftItemFactory#enchantWithLevels} (enchanting-table enchantments unless {@code allowTreasure}). */
    public static ItemStack enchantWithLevels(ItemStack itemStack, int levels, boolean allowTreasure, java.util.Random random) {
        final net.minecraft.core.RegistryAccess registryAccess = ArclightServer.getMinecraftServer().registryAccess();
        // While IN_ENCHANTING_TABLE is not logically the same as all but TREASURE, the tag is defined as
        // NON_TREASURE, which does contain all enchantments not in the treasure tag.
        Optional<? extends net.minecraft.core.HolderSet<net.minecraft.world.item.enchantment.Enchantment>> possible = allowTreasure
            ? Optional.empty()
            : registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getTag(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE);
        return enchantWithLevels(itemStack, levels, possible, random);
    }

    /** Paper's {@code CraftItemFactory#enchantWithLevels} with a registry set (patch "Add enchantWithLevels with enchantment registry set"). */
    public static ItemStack enchantWithLevels(ItemStack itemStack, int levels, io.papermc.paper.registry.set.RegistryKeySet<org.bukkit.enchantments.Enchantment> keySet, java.util.Random random) {
        return enchantWithLevels(itemStack, levels, Optional.of(io.papermc.paper.registry.set.PaperRegistrySets.convertToNms(net.minecraft.core.registries.Registries.ENCHANTMENT, keySet)), random);
    }

    private static ItemStack enchantWithLevels(ItemStack itemStack, int levels, Optional<? extends net.minecraft.core.HolderSet<net.minecraft.world.item.enchantment.Enchantment>> possible, java.util.Random random) {
        com.google.common.base.Preconditions.checkArgument(itemStack != null, "Argument 'itemStack' must not be null");
        com.google.common.base.Preconditions.checkArgument(!itemStack.getType().isAir() && itemStack.getAmount() > 0, "Argument 'itemStack' cannot be empty");
        com.google.common.base.Preconditions.checkArgument(levels > 0 && levels <= 30, "Argument 'levels' must be in range [1, 30] (attempted " + levels + ")");
        com.google.common.base.Preconditions.checkArgument(random != null, "Argument 'random' must not be null");
        final net.minecraft.core.RegistryAccess registryAccess = ArclightServer.getMinecraftServer().registryAccess();
        final net.minecraft.world.item.ItemStack internalStack = CraftItemStack.asNMSCopy(itemStack);
        if (internalStack.isEnchanted()) {
            internalStack.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        }
        final net.minecraft.world.item.ItemStack enchanted = net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(
            new org.bukkit.craftbukkit.v.util.RandomSourceWrapper(random), internalStack, levels, registryAccess, possible);
        return CraftItemStack.asCraftMirror(enchanted);
    }

    /** Paper's {@code CraftMagicNumbers#computeTooltipLines}: the tooltip vanilla would show, mods' additions included. */
    public static List<Component> computeTooltipLines(ItemStack itemStack, io.papermc.paper.inventory.tooltip.TooltipContext tooltipContext, org.bukkit.entity.Player player) {
        com.google.common.base.Preconditions.checkArgument(tooltipContext != null, "tooltipContext cannot be null");
        net.minecraft.world.item.TooltipFlag.Default flag = tooltipContext.isAdvanced() ? net.minecraft.world.item.TooltipFlag.ADVANCED : net.minecraft.world.item.TooltipFlag.NORMAL;
        if (tooltipContext.isCreative()) {
            flag = flag.asCreative();
        }
        net.minecraft.server.level.ServerPlayer handle = player == null ? null : ((org.bukkit.craftbukkit.v.entity.CraftPlayer) player).getHandle();
        final List<net.minecraft.network.chat.Component> lines = CraftItemStack.asNMSCopy(itemStack).getTooltipLines(
            net.minecraft.world.item.Item.TooltipContext.of(handle == null ? ArclightServer.getMinecraftServer().registryAccess() : handle.level().registryAccess()),
            handle, flag);
        return lines.stream().map(FusionCoreAdventure::asAdventure).toList();
    }

    /**
     * Paper's {@code CraftMagicNumbers#serializeItemAsJson}: the item through vanilla's codec with "DataVersion"; custom data
     * components are written as SNBT strings, as Paper does, so their exact NBT types survive the round trip.
     */
    public static com.google.gson.JsonObject serializeItemAsJson(ItemStack itemStack) {
        com.google.common.base.Preconditions.checkNotNull(itemStack, "Cannot serialize empty ItemStack");
        com.google.common.base.Preconditions.checkArgument(!itemStack.getType().isAir() && itemStack.getAmount() > 0, "Cannot serialize empty ItemStack");
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(itemStack);
        com.mojang.serialization.DynamicOps<JsonElement> ops = ArclightServer.getMinecraftServer().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        com.google.gson.JsonObject item = net.minecraft.world.item.ItemStack.CODEC.encodeStart(ops, nms).getOrThrow().getAsJsonObject();
        if (item.get("components") instanceof com.google.gson.JsonObject components) {
            for (var entry : nms.getComponentsPatch().entrySet()) {
                if (entry.getValue().orElse(null) instanceof net.minecraft.world.item.component.CustomData data) {
                    ResourceLocation key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(entry.getKey());
                    if (key != null && components.has(key.toString())) {
                        components.addProperty(key.toString(), data.copyTag().toString());
                    }
                }
            }
        }
        item.addProperty("DataVersion", net.minecraft.SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        return item;
    }

    /** Paper's {@code CraftMagicNumbers#deserializeItemFromJson}; older data is updated with vanilla's DataFixer. */
    public static ItemStack deserializeItemFromJson(com.google.gson.JsonObject data) {
        com.google.common.base.Preconditions.checkNotNull(data, "null cannot be deserialized");
        final int dataVersion = data.get("DataVersion").getAsInt();
        final int currentVersion = net.minecraft.SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        JsonElement json = data;
        if (dataVersion < currentVersion) {
            json = net.minecraft.util.datafix.DataFixers.getDataFixer().update(net.minecraft.util.datafix.fixes.References.ITEM_STACK,
                new com.mojang.serialization.Dynamic<>(JsonOps.INSTANCE, (JsonElement) data), dataVersion, currentVersion).getValue();
        }
        com.mojang.serialization.DynamicOps<JsonElement> ops = ArclightServer.getMinecraftServer().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        return CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.CODEC.parse(ops, json).getOrThrow(IllegalArgumentException::new));
    }
}
