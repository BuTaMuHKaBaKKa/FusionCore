package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.UnsafeValues} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.UnsafeValues.class, remap = false)
public interface UnsafeValuesMixin {

    // the bodies live in PaperComponents, which plugins call directly

    default net.kyori.adventure.text.flattener.ComponentFlattener componentFlattener() {
        return io.papermc.paper.text.PaperComponents.flattener();
    }

    @SuppressWarnings({"deprecation", "removal"})
    default net.kyori.adventure.text.serializer.plain.PlainComponentSerializer plainComponentSerializer() {
        return io.papermc.paper.text.PaperComponents.plainSerializer();
    }

    @SuppressWarnings("removal")
    default net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer plainTextSerializer() {
        return io.papermc.paper.text.PaperComponents.plainTextSerializer();
    }

    @SuppressWarnings("removal")
    default net.kyori.adventure.text.serializer.gson.GsonComponentSerializer gsonComponentSerializer() {
        return io.papermc.paper.text.PaperComponents.gsonSerializer();
    }

    @SuppressWarnings("removal")
    default net.kyori.adventure.text.serializer.gson.GsonComponentSerializer colorDownsamplingGsonComponentSerializer() {
        return io.papermc.paper.text.PaperComponents.colorDownsamplingGsonSerializer();
    }

    @SuppressWarnings("removal")
    default net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer legacyComponentSerializer() {
        return io.papermc.paper.text.PaperComponents.legacySectionSerializer();
    }

    /** Paper: resolves selectors, scores and NBT in a component as the vanilla command would, for that sender. */
    default net.kyori.adventure.text.Component resolveWithContext(net.kyori.adventure.text.Component component, org.bukkit.command.CommandSender context, org.bukkit.entity.Entity scoreboardSubject, boolean bypassPermissions) throws java.io.IOException {
        return io.papermc.paper.text.PaperComponents.resolveWithContext(component, context, scoreboardSubject, bypassPermissions);
    }

    default byte[] serializeItem(org.bukkit.inventory.ItemStack item) {
        return io.izzel.arclight.common.mod.util.FusionCoreItems.serializeItem(item);
    }

    default org.bukkit.inventory.ItemStack deserializeItem(byte[] data) {
        return io.izzel.arclight.common.mod.util.FusionCoreItems.deserializeItem(data);
    }

    /** Paper: the protocol version of the Minecraft version this server runs. */
    default int getProtocolVersion() {
        return net.minecraft.SharedConstants.getCurrentVersion().getProtocolVersion();
    }

    /** Paper: the key of the biome at a block, custom (datapack/mod) biomes included. */
    default org.bukkit.NamespacedKey getBiomeKey(org.bukkit.RegionAccessor accessor, int x, int y, int z) {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.biomeKey(accessor, x, y, z);
    }

    // Fusion Core: Paper's item helpers ("ItemStack repair check API", "ItemStack Tooltip API", "Item serialization as json",
    // "ItemStack#empty"); bodies in FusionCoreItems.

    default boolean isValidRepairItemStack(org.bukkit.inventory.ItemStack itemToBeRepaired, org.bukkit.inventory.ItemStack repairMaterial) {
        return io.izzel.arclight.common.mod.util.FusionCoreItems.isValidRepairItemStack(itemToBeRepaired, repairMaterial);
    }

    default java.util.List<net.kyori.adventure.text.Component> computeTooltipLines(org.bukkit.inventory.ItemStack itemStack, io.papermc.paper.inventory.tooltip.TooltipContext tooltipContext, org.bukkit.entity.Player player) {
        return io.izzel.arclight.common.mod.util.FusionCoreItems.computeTooltipLines(itemStack, tooltipContext, player);
    }

    default com.google.gson.JsonObject serializeItemAsJson(org.bukkit.inventory.ItemStack itemStack) {
        return io.izzel.arclight.common.mod.util.FusionCoreItems.serializeItemAsJson(itemStack);
    }

    default org.bukkit.inventory.ItemStack deserializeItemFromJson(com.google.gson.JsonObject data) throws IllegalArgumentException {
        return io.izzel.arclight.common.mod.util.FusionCoreItems.deserializeItemFromJson(data);
    }

    default org.bukkit.inventory.ItemStack createEmptyStack() {
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(null);
    }

    // Fusion Core: Paper's default entity attributes (patch 0584)
    default boolean hasDefaultEntityAttributes(org.bukkit.NamespacedKey bukkitEntityKey) {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.hasDefaultEntityAttributes(bukkitEntityKey);
    }

    default org.bukkit.attribute.Attributable getDefaultEntityAttributes(org.bukkit.NamespacedKey bukkitEntityKey) {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.defaultEntityAttributes(bukkitEntityKey);
    }

    // Fusion Core: the rest of Paper's UnsafeValues (bodies in FusionCoreUnsafeValues)

    default byte[] serializeEntity(org.bukkit.entity.Entity entity) {
        return io.izzel.arclight.common.mod.util.FusionCoreUnsafeValues.serializeEntity(entity);
    }

    default org.bukkit.entity.Entity deserializeEntity(byte[] data, org.bukkit.World world) {
        return io.izzel.arclight.common.mod.util.FusionCoreUnsafeValues.deserializeEntity(data, world, false);
    }

    default org.bukkit.entity.Entity deserializeEntity(byte[] data, org.bukkit.World world, boolean preserveUUID) {
        return io.izzel.arclight.common.mod.util.FusionCoreUnsafeValues.deserializeEntity(data, world, preserveUUID);
    }

    default String getMainLevelName() {
        return io.izzel.arclight.common.mod.util.FusionCoreUnsafeValues.getMainLevelName();
    }

    default org.bukkit.Color getSpawnEggLayerColor(org.bukkit.entity.EntityType entityType, int layer) {
        return io.izzel.arclight.common.mod.util.FusionCoreUnsafeValues.getSpawnEggLayerColor(entityType, layer);
    }

    default String getStatisticCriteriaKey(org.bukkit.Statistic statistic) {
        return io.izzel.arclight.common.mod.util.FusionCoreUnsafeValues.getStatisticCriteriaKey(statistic);
    }

    default int nextEntityId() {
        return io.izzel.arclight.common.mod.util.FusionCoreUnsafeValues.nextEntityId();
    }

    default boolean isSupportedApiVersion(String apiVersion) {
        return io.izzel.arclight.common.mod.util.FusionCoreUnsafeValues.isSupportedApiVersion(apiVersion);
    }

    default void setBiomeKey(org.bukkit.RegionAccessor accessor, int x, int y, int z, org.bukkit.NamespacedKey biomeKey) {
        io.izzel.arclight.common.mod.util.FusionCoreUnsafeValues.setBiomeKey(accessor, x, y, z, biomeKey);
    }

    /** Paper checks PaperMC for updates; Fusion Core reports its build without a remote check. */
    default com.destroystokyo.paper.util.VersionFetcher getVersionFetcher() {
        return io.izzel.arclight.common.mod.util.FusionCoreUnsafeValues.VERSION_FETCHER;
    }

    /** Fusion Core: Paper's lifecycle event manager of a {@code JavaPlugin} (patch 0926), as in {@code CraftMagicNumbers}. */
    @SuppressWarnings({"unchecked", "rawtypes"}) // <Plugin> in Paper; javac does not see Plugin as a LifecycleEventOwner
    default io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager createPluginLifecycleEventManager(
            final org.bukkit.plugin.java.JavaPlugin plugin, final java.util.function.BooleanSupplier registrationCheck) {
        return new io.papermc.paper.plugin.lifecycle.event.PaperLifecycleEventManager((io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner) (Object) plugin, registrationCheck);
    }

    /** Paper (patch "Registry Modification API"): tags of registries without a server-backed API registry. */
    default <A extends org.bukkit.Keyed> io.papermc.paper.registry.tag.Tag<A> getTag(io.papermc.paper.registry.tag.TagKey<A> tagKey) {
        return io.izzel.arclight.common.mod.util.FusionCoreRegistries.tag(tagKey);
    }

    /** Paper (timings): the server name in a timings report; Paper's default setting. */
    default String getTimingsServerName() {
        return "Unknown Server";
    }

    /** Paper (timings): timings are not available on Fusion Core, so the report only says so, as {@code /timings report} does. */
    default void reportTimings() {
        co.aikar.timings.Timings.generateReport((org.bukkit.command.CommandSender) null);
    }
}
