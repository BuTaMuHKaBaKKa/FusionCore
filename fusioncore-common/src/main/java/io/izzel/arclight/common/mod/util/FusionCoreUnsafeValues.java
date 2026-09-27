package io.izzel.arclight.common.mod.util;

import com.google.common.base.Preconditions;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.entity.Entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Fusion Core: bodies of Paper's {@code UnsafeValues} additions, called from {@code UnsafeValuesMixin} and, for the
 * interface's static {@code isLegacyPlugin}, from {@link FusionCoreUnsafeValuesStatics}. Each method names its Paper patch.
 */
public final class FusionCoreUnsafeValues {

    private FusionCoreUnsafeValues() {
    }

    private static int dataVersion() {
        return net.minecraft.SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    /**
     * Paper patch 0593: the entity's NBT without its passengers, with "DataVersion", gzip-compressed. Paper saves with the
     * passenger list emptied; dropping the "Passengers" tag from the save gives the same data.
     */
    public static byte[] serializeEntity(Entity entity) {
        Preconditions.checkNotNull(entity, "null cannot be serialized");
        Preconditions.checkArgument(entity instanceof CraftEntity, "only CraftEntities can be serialized");
        CompoundTag compound = new CompoundTag();
        ((CraftEntity) entity).getHandle().save(compound);
        compound.remove("Passengers");
        compound.putInt("DataVersion", dataVersion());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            NbtIo.writeCompressed(compound, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    /** Paper patch 0593: a new entity (not added to the world) from {@link #serializeEntity} data, updated by the DataFixer. */
    public static Entity deserializeEntity(byte[] data, org.bukkit.World world, boolean preserveUUID) {
        Preconditions.checkNotNull(data, "null cannot be deserialized");
        Preconditions.checkArgument(data.length > 0, "cannot deserialize nothing");
        CompoundTag compound;
        try {
            compound = NbtIo.readCompressed(new ByteArrayInputStream(data), NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int version = compound.getInt("DataVersion");
        compound = (CompoundTag) ArclightServer.getMinecraftServer().getFixerUpper().update(net.minecraft.util.datafix.fixes.References.ENTITY,
            new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, compound), version, dataVersion()).getValue();
        if (!preserveUUID) {
            // Generate a new UUID so we don't have to worry about deserializing the same entity twice
            compound.remove("UUID");
        }
        net.minecraft.world.entity.Entity nms = net.minecraft.world.entity.EntityType.create(compound, ((CraftWorld) world).getHandle())
            .orElseThrow(() -> new IllegalArgumentException("An ID was not found for the data. Did you downgrade?"));
        return ((EntityBridge) nms).bridge$getBukkitEntity();
    }

    /** Paper patch 0500: the level name from server.properties. */
    public static String getMainLevelName() {
        return ((net.minecraft.server.dedicated.DedicatedServer) ArclightServer.getMinecraftServer()).getProperties().levelName;
    }

    /** Paper patch 0925: a layer colour of the type's spawn egg (mod eggs included), or null if it has none. */
    public static org.bukkit.Color getSpawnEggLayerColor(org.bukkit.entity.EntityType entityType, int layer) {
        net.minecraft.world.entity.EntityType<?> nmsType = org.bukkit.craftbukkit.v.entity.CraftEntityType.bukkitToMinecraft(entityType);
        net.minecraft.world.item.SpawnEggItem eggItem = net.minecraft.world.item.SpawnEggItem.byId(nmsType);
        return eggItem == null ? null : org.bukkit.Color.fromRGB(eggItem.getColor(layer) & 0xFFFFFF);
    }

    /** Paper patch 0853: the scoreboard criteria name of a statistic. */
    public static String getStatisticCriteriaKey(org.bukkit.Statistic statistic) {
        if (statistic.getType() != org.bukkit.Statistic.Type.UNTYPED) {
            return "minecraft.custom:minecraft." + statistic.getKey().getKey();
        }
        return org.bukkit.craftbukkit.v.CraftStatistic.getNMSStatistic(statistic).getName();
    }

    /** Paper patch 0411: the next id from vanilla's entity counter (what a new entity would take). */
    public static int nextEntityId() {
        return net.minecraft.world.entity.Entity.ENTITY_COUNTER.incrementAndGet();
    }

    /** Paper patch 0019: whether a plugin with this api-version loads here (not newer than the server, not below minimum-api). */
    public static boolean isSupportedApiVersion(String apiVersion) {
        if (apiVersion == null) {
            return false;
        }
        org.bukkit.craftbukkit.v.util.ApiVersion toCheck = org.bukkit.craftbukkit.v.util.ApiVersion.getOrCreateVersion(apiVersion);
        org.bukkit.craftbukkit.v.util.ApiVersion minimum = ((org.bukkit.craftbukkit.v.CraftServer) Bukkit.getServer()).minimumAPI;
        return !toCheck.isNewerThan(org.bukkit.craftbukkit.v.util.ApiVersion.CURRENT) && !toCheck.isOlderThan(minimum);
    }

    /** Paper patch 0732: sets the biome at a block by key; datapack and mod biomes included. */
    public static void setBiomeKey(org.bukkit.RegionAccessor accessor, int x, int y, int z, org.bukkit.NamespacedKey biomeKey) {
        org.bukkit.craftbukkit.v.CraftRegionAccessor cra = (org.bukkit.craftbukkit.v.CraftRegionAccessor) accessor;
        net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biome = cra.getHandle().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
            .getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME, org.bukkit.craftbukkit.v.util.CraftNamespacedKey.toMinecraft(biomeKey)));
        cra.setBiome(x, y, z, biome);
    }

    /** Fusion Core's answer to Paper's version fetcher: no remote update check, the same text as {@code /version}. */
    public static final com.destroystokyo.paper.util.VersionFetcher VERSION_FETCHER = new com.destroystokyo.paper.util.VersionFetcher() {
        @Override
        public long getCacheTime() {
            return 720000;
        }

        @Override
        public net.kyori.adventure.text.Component getVersionMessage(String serverVersion) {
            String version = System.getProperty("arclight.version");
            return net.kyori.adventure.text.Component.text("Fusion Core " + (version == null ? "unknown build" : version) + ": update checks are not available.",
                net.kyori.adventure.text.format.NamedTextColor.GRAY);
        }
    };
}
