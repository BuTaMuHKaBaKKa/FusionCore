package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.server.ArclightServer;
import io.papermc.paper.world.flag.FeatureDependant;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.bukkit.FeatureFlag;
import org.bukkit.craftbukkit.v.entity.CraftEntityType;
import org.bukkit.craftbukkit.v.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v.potion.CraftPotionType;
import org.bukkit.craftbukkit.v.util.Handleable;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Set;
import java.util.stream.Collectors;

/** Fusion Core: Paper's feature flag API on types and worlds; flags are the canonical API objects. */
public final class FusionCoreFeatureFlags {

    private FusionCoreFeatureFlags() {
    }

    public static Set<FeatureFlag> requiredFeatures(FeatureDependant dependant) {
        return fromNms(element(dependant).requiredFeatures());
    }

    public static Set<FeatureFlag> fromNms(FeatureFlagSet flags) {
        net.kyori.adventure.util.Index<net.kyori.adventure.key.Key, FeatureFlag> all = FusionCoreEnumConstants.FeatureFlag$ALL_FLAGS();
        return FeatureFlags.REGISTRY.toNames(flags).stream()
            .map(name -> all.valueOrThrow(net.kyori.adventure.key.Key.key(name.getNamespace(), name.getPath())))
            .collect(Collectors.toUnmodifiableSet());
    }

    /** The server's enabled features: vanilla has one feature set for all worlds (the one of the world data). */
    public static Set<FeatureFlag> serverFeatures() {
        return fromNms(ArclightServer.getMinecraftServer().getWorldData().enabledFeatures());
    }

    private static FeatureElement element(Object dependant) {
        if (dependant instanceof Handleable<?> handleable && handleable.getHandle() instanceof FeatureElement element) {
            return element;
        } else if (dependant instanceof EntityType entityType) {
            return CraftEntityType.bukkitToMinecraft(entityType);
        } else if (dependant instanceof PotionType potionType) {
            return CraftPotionType.bukkitToMinecraft(potionType);
        } else if (dependant instanceof PotionEffectType effectType) {
            return CraftPotionEffectType.bukkitToMinecraft(effectType);
        }
        throw new IllegalArgumentException(dependant + " is not a valid feature dependant");
    }
}
