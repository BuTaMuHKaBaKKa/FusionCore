package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.server.ArclightServer;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.NamedRegistryKeySetImpl;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/** Fusion Core: maps Paper's {@link RegistryKey}s to the vanilla registries and Bukkit's {@code Registry} constants. */
public final class FusionCoreRegistries {

    private static final Map<RegistryKey<?>, Entry> BY_KEY = new IdentityHashMap<>();
    private static final Map<Class<?>, RegistryKey<?>> BY_TYPE = new HashMap<>();
    private static final Map<ResourceLocation, RegistryKey<?>> BY_VANILLA = new HashMap<>();

    // the Bukkit registry is read when asked for: org.bukkit.Registry needs the server, and Paper plugins are bootstrapped before it
    private record Entry(Class<?> type, java.util.function.Supplier<org.bukkit.Registry<?>> bukkit) {
        org.bukkit.Registry<?> registry() {
            return this.bukkit.get();
        }
    }

    static {
        put(RegistryKey.GAME_EVENT, org.bukkit.GameEvent.class, () -> org.bukkit.Registry.GAME_EVENT);
        put(RegistryKey.STRUCTURE_TYPE, org.bukkit.generator.structure.StructureType.class, () -> org.bukkit.Registry.STRUCTURE_TYPE);
        put(RegistryKey.INSTRUMENT, org.bukkit.MusicInstrument.class, () -> org.bukkit.Registry.INSTRUMENT);
        put(RegistryKey.MOB_EFFECT, org.bukkit.potion.PotionEffectType.class, () -> org.bukkit.Registry.EFFECT);
        put(RegistryKey.BLOCK, org.bukkit.block.BlockType.class, () -> org.bukkit.Registry.BLOCK);
        put(RegistryKey.ITEM, org.bukkit.inventory.ItemType.class, () -> org.bukkit.Registry.ITEM);
        put(RegistryKey.CAT_VARIANT, org.bukkit.entity.Cat.Type.class, () -> org.bukkit.Registry.CAT_VARIANT);
        put(RegistryKey.FROG_VARIANT, org.bukkit.entity.Frog.Variant.class, () -> org.bukkit.Registry.FROG_VARIANT);
        put(RegistryKey.VILLAGER_PROFESSION, org.bukkit.entity.Villager.Profession.class, () -> org.bukkit.Registry.VILLAGER_PROFESSION);
        put(RegistryKey.VILLAGER_TYPE, org.bukkit.entity.Villager.Type.class, () -> org.bukkit.Registry.VILLAGER_TYPE);
        put(RegistryKey.MAP_DECORATION_TYPE, org.bukkit.map.MapCursor.Type.class, () -> org.bukkit.Registry.MAP_DECORATION_TYPE);
        put(RegistryKey.MENU, org.bukkit.inventory.MenuType.class, () -> org.bukkit.Registry.MENU);
        put(RegistryKey.BIOME, org.bukkit.block.Biome.class, () -> org.bukkit.Registry.BIOME);
        put(RegistryKey.STRUCTURE, org.bukkit.generator.structure.Structure.class, () -> org.bukkit.Registry.STRUCTURE);
        put(RegistryKey.TRIM_MATERIAL, org.bukkit.inventory.meta.trim.TrimMaterial.class, () -> org.bukkit.Registry.TRIM_MATERIAL);
        put(RegistryKey.TRIM_PATTERN, org.bukkit.inventory.meta.trim.TrimPattern.class, () -> org.bukkit.Registry.TRIM_PATTERN);
        put(RegistryKey.DAMAGE_TYPE, org.bukkit.damage.DamageType.class, () -> org.bukkit.Registry.DAMAGE_TYPE);
        put(RegistryKey.WOLF_VARIANT, org.bukkit.entity.Wolf.Variant.class, () -> org.bukkit.Registry.WOLF_VARIANT);
        put(RegistryKey.ENCHANTMENT, org.bukkit.enchantments.Enchantment.class, () -> org.bukkit.Registry.ENCHANTMENT);
        put(RegistryKey.JUKEBOX_SONG, org.bukkit.JukeboxSong.class, () -> org.bukkit.Registry.JUKEBOX_SONG);
        put(RegistryKey.BANNER_PATTERN, org.bukkit.block.banner.PatternType.class, () -> org.bukkit.Registry.BANNER_PATTERN);
        put(RegistryKey.PAINTING_VARIANT, org.bukkit.Art.class, () -> org.bukkit.Registry.ART);
        put(RegistryKey.ATTRIBUTE, org.bukkit.attribute.Attribute.class, () -> org.bukkit.Registry.ATTRIBUTE);
        put(RegistryKey.ENTITY_TYPE, org.bukkit.entity.EntityType.class, () -> org.bukkit.Registry.ENTITY_TYPE);
        put(RegistryKey.PARTICLE_TYPE, org.bukkit.Particle.class, () -> org.bukkit.Registry.PARTICLE_TYPE);
        put(RegistryKey.POTION, org.bukkit.potion.PotionType.class, () -> org.bukkit.Registry.POTION);
        put(RegistryKey.SOUND_EVENT, org.bukkit.Sound.class, () -> org.bukkit.Registry.SOUNDS);
        put(RegistryKey.MEMORY_MODULE_TYPE, org.bukkit.entity.memory.MemoryKey.class, () -> org.bukkit.Registry.MEMORY_MODULE_TYPE);
        put(RegistryKey.FLUID, org.bukkit.Fluid.class, () -> org.bukkit.Registry.FLUID);
    }

    private static void put(RegistryKey<?> key, Class<?> type, java.util.function.Supplier<org.bukkit.Registry<?>> registry) {
        BY_KEY.put(key, new Entry(type, registry));
        BY_TYPE.put(type, key);
        BY_VANILLA.put(ResourceLocation.fromNamespaceAndPath(key.key().namespace(), key.key().value()), key);
    }

    private FusionCoreRegistries() {
    }

    /** The vanilla registry named by {@code key}: Paper's keys carry vanilla's registry ids. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <M> ResourceKey<? extends Registry<M>> toNms(RegistryKey<?> key) {
        return (ResourceKey) ResourceKey.createRegistryKey(location(key.key()));
    }

    /** Paper's {@code PaperRegistries#registryFromNms}: the API key of a vanilla registry, {@code null} when the API has none. */
    @SuppressWarnings("unchecked")
    public static <T> RegistryKey<T> fromNms(ResourceKey<? extends Registry<?>> registry) {
        return (RegistryKey<T>) BY_VANILLA.get(registry.location());
    }

    /** Paper's {@code PaperRegistries#fromNms}: a vanilla entry key as a typed key. */
    @SuppressWarnings("unchecked")
    public static <T> TypedKey<T> typedKey(ResourceKey<?> entry) {
        RegistryKey<T> registryKey = fromNms((ResourceKey<? extends Registry<?>>) (Object) ResourceKey.createRegistryKey(entry.registry()));
        if (registryKey == null) {
            throw new IllegalArgumentException("No API registry for " + entry.registry());
        }
        return TypedKey.create(registryKey, net.kyori.adventure.key.Key.key(entry.location().getNamespace(), entry.location().getPath()));
    }

    /** Paper's {@code PaperRegistries#toNms(TypedKey)}. */
    public static <M> ResourceKey<M> toNms(TypedKey<?> key) {
        return ResourceKey.create(FusionCoreRegistries.<M>toNms(key.registryKey()), location(key.key()));
    }

    /** Paper's {@code PaperRegistries#toNms(TagKey)}. */
    public static <M> net.minecraft.tags.TagKey<M> toNms(io.papermc.paper.registry.tag.TagKey<?> key) {
        return net.minecraft.tags.TagKey.create(FusionCoreRegistries.<M>toNms(key.registryKey()), location(key.key()));
    }

    /** Paper's {@code PaperRegistries#fromNms(TagKey)}. */
    public static <T> io.papermc.paper.registry.tag.TagKey<T> fromNms(net.minecraft.tags.TagKey<?> key) {
        RegistryKey<T> registryKey = fromNms(key.registry());
        if (registryKey == null) {
            throw new IllegalArgumentException("No API registry for " + key.registry().location());
        }
        return io.papermc.paper.registry.tag.TagKey.create(registryKey, net.kyori.adventure.key.Key.key(key.location().getNamespace(), key.location().getPath()));
    }

    /** A vanilla id as an Adventure key ({@code CraftNamespacedKey#fromMinecraft} in Paper, where NamespacedKey is a Key). */
    public static net.kyori.adventure.key.Key key(ResourceLocation location) {
        return net.kyori.adventure.key.Key.key(location.getNamespace(), location.getPath());
    }

    public static ResourceLocation location(net.kyori.adventure.key.Key key) {
        return ResourceLocation.fromNamespaceAndPath(key.namespace(), key.value());
    }

    /** The server's vanilla registry named by {@code key} (built-in or data-driven). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <M> Registry<M> vanillaRegistry(RegistryKey<?> key) {
        return (Registry<M>) ArclightServer.getMinecraftServer().registryAccess().registryOrThrow((ResourceKey) toNms(key));
    }

    /** Bukkit's registry of the API values for {@code key}. */
    public static org.bukkit.Registry<?> bukkitRegistry(RegistryKey<?> key) {
        Entry entry = BY_KEY.get(key);
        if (entry == null) {
            throw new java.util.NoSuchElementException(key + " is not a valid registry key");
        }
        return entry.registry();
    }

    /** Paper's deprecated {@code RegistryAccess#getRegistry(Class)}: {@code null} for a type without a registry. */
    public static org.bukkit.Registry<?> bukkitRegistry(Class<?> type) {
        RegistryKey<?> key = BY_TYPE.get(type);
        return key == null ? null : BY_KEY.get(key).registry();
    }

    /** The registry key whose Bukkit registry is {@code registry}, or {@code null}. */
    public static RegistryKey<?> keyOf(org.bukkit.Registry<?> registry) {
        for (Map.Entry<RegistryKey<?>, Entry> e : BY_KEY.entrySet()) {
            if (e.getValue().registry() == registry) {
                return e.getKey();
            }
        }
        return null;
    }

    /** {@code Registry#getKey(value)} (added by a mixin, so javac does not see it). */
    public static NamespacedKey keyOf(org.bukkit.Registry<?> registry, Keyed value) {
        return value.getKey();
    }

    /** {@code Registry#get(Key)} (added by a mixin, so javac does not see it). */
    public static <T extends Keyed> T get(org.bukkit.Registry<T> registry, net.kyori.adventure.key.Key key) {
        return registry.get(new NamespacedKey(key.namespace(), key.value()));
    }

    /** Paper's {@code UnsafeValues#getTag}: the vanilla tag behind {@code tagKey}, or {@code null}. */
    public static <T extends Keyed, M> io.papermc.paper.registry.tag.Tag<T> tag(io.papermc.paper.registry.tag.TagKey<T> tagKey) {
        if (!BY_KEY.containsKey(tagKey.registryKey()) || tagKey.registryKey() == RegistryKey.MEMORY_MODULE_TYPE) {
            throw new UnsupportedOperationException(tagKey.registryKey() + " doesn't have tags");
        }
        Registry<M> registry = vanillaRegistry(tagKey.registryKey());
        return registry.getTag(FusionCoreRegistries.<M>toNms(tagKey))
            .<io.papermc.paper.registry.tag.Tag<T>>map(named -> new NamedRegistryKeySetImpl<>(tagKey, named))
            .orElse(null);
    }

    /** Paper's {@code CraftRegistry#getTag}: the tag from the vanilla registry behind a {@code CraftRegistry}. */
    public static <T extends Keyed, M> io.papermc.paper.registry.tag.Tag<T> tag(Registry<M> registry, io.papermc.paper.registry.tag.TagKey<T> tagKey) {
        HolderSet.Named<M> named = registry.getTag(net.minecraft.tags.TagKey.create(registry.key(), location(tagKey.key()))).orElseThrow();
        return new NamedRegistryKeySetImpl<>(tagKey, named);
    }

    public static <M> boolean hasTag(Registry<M> registry, io.papermc.paper.registry.tag.TagKey<?> tagKey) {
        return registry.getTag(net.minecraft.tags.TagKey.create(registry.key(), location(tagKey.key()))).isPresent();
    }
}
