package com.destroystokyo.paper.profile;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.ProfileResult;
import io.izzel.arclight.common.mixin.bukkit.CraftPlayerProfileAccessor;
import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.common.mod.util.FusionCoreProfiles;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.component.ResolvableProfile;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Fusion Core: port of Paper's {@code CraftPlayerProfile} (patches 0136, 0176). Textures go through a Spigot profile
 * that shares this profile's property map; forwarded players count as online-mode.
 */
@SerializableAs("PlayerProfile")
public class CraftPlayerProfile implements PlayerProfile {

    private boolean emptyName;
    private boolean emptyUUID;
    private GameProfile profile;
    private final PropertySet properties = new PropertySet();

    public CraftPlayerProfile(CraftPlayer player) {
        this.profile = player.getHandle().getGameProfile();
    }

    public CraftPlayerProfile(UUID id, String name) {
        this.profile = createAuthLibProfile(id, name);
        this.emptyName = name == null;
        this.emptyUUID = id == null;
    }

    public CraftPlayerProfile(GameProfile profile) {
        Preconditions.checkArgument(profile != null, "GameProfile cannot be null!");
        this.profile = profile;
    }

    public CraftPlayerProfile(ResolvableProfile resolvableProfile) {
        this(resolvableProfile.id().orElse(null), resolvableProfile.name().orElse(null));
        copyProfileProperties(resolvableProfile.gameProfile(), this.profile);
    }

    @Override
    public boolean hasProperty(String property) {
        return profile.getProperties().containsKey(property);
    }

    @Override
    public void setProperty(ProfileProperty property) {
        String name = property.getName();
        PropertyMap properties = profile.getProperties();
        properties.removeAll(name);

        Preconditions.checkArgument(properties.size() < 16, "Cannot add more than 16 properties to a profile");
        properties.put(name, new Property(name, property.getValue(), property.getSignature()));
    }

    /**
     * Spigot's profile over this profile's property map: its textures, serialized form and string form are
     * this profile's (see the class javadoc).
     */
    private org.bukkit.craftbukkit.v.profile.CraftPlayerProfile spigotView() {
        org.bukkit.craftbukkit.v.profile.CraftPlayerProfile view = new org.bukkit.craftbukkit.v.profile.CraftPlayerProfile(
            new ResolvableProfile(Optional.ofNullable(this.getName()), Optional.ofNullable(this.getId()), new PropertyMap()));
        ((CraftPlayerProfileAccessor) (Object) view).fusioncore$setProperties(this.profile.getProperties());
        return view;
    }

    @Override
    public PlayerTextures getTextures() {
        return this.spigotView().getTextures();
    }

    @Override
    public void setTextures(@Nullable PlayerTextures textures) {
        if (textures == null) {
            this.removeProperty("textures");
        } else {
            org.bukkit.craftbukkit.v.profile.CraftPlayerProfile view = this.spigotView();
            view.setTextures(textures);
            view.buildResolvableProfile(); // rebuilds the textures property if it changed
        }
    }

    public GameProfile getGameProfile() {
        return profile;
    }

    @Nullable
    @Override
    public UUID getId() {
        return this.emptyUUID ? null : this.profile.getId();
    }

    @Override
    @Deprecated(forRemoval = true)
    public UUID setId(@Nullable UUID uuid) {
        final GameProfile previousProfile = this.profile;
        final UUID previousId = this.getId();
        this.profile = createAuthLibProfile(uuid, previousProfile.getName());
        copyProfileProperties(previousProfile, this.profile);
        this.emptyUUID = uuid == null;
        return previousId;
    }

    @Override
    public UUID getUniqueId() {
        return getId();
    }

    @Nullable
    @Override
    public String getName() {
        return this.emptyName ? null : this.profile.getName();
    }

    @Override
    @Deprecated(forRemoval = true)
    public String setName(@Nullable String name) {
        GameProfile prev = this.profile;
        this.profile = createAuthLibProfile(prev.getId(), name);
        copyProfileProperties(prev, this.profile);
        this.emptyName = name == null;
        return prev.getName();
    }

    @NotNull
    @Override
    public Set<ProfileProperty> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Collection<ProfileProperty> properties) {
        properties.forEach(this::setProperty);
    }

    @Override
    public void clearProperties() {
        profile.getProperties().clear();
    }

    @Override
    public boolean removeProperty(String property) {
        return !profile.getProperties().removeAll(property).isEmpty();
    }

    @Nullable
    public Property getProperty(String property) {
        return Iterables.getFirst(this.profile.getProperties().get(property), null);
    }

    public void setProperty(@NotNull String propertyName, @Nullable Property property) {
        if (property != null) {
            this.setProperty(new ProfileProperty(propertyName, property.value(), property.signature()));
        } else {
            profile.getProperties().removeAll(propertyName);
        }
    }

    public @NotNull GameProfile buildGameProfile() {
        GameProfile profile = new GameProfile(this.profile.getId(), this.profile.getName());
        profile.getProperties().putAll(this.profile.getProperties());
        return profile;
    }

    public @NotNull ResolvableProfile buildResolvableProfile() {
        if (this.emptyName || this.emptyUUID) {
            return new ResolvableProfile(this.emptyName ? Optional.empty() : Optional.of(this.profile.getName()), this.emptyUUID ? Optional.empty() : Optional.of(this.profile.getId()), this.profile.getProperties());
        } else {
            return new ResolvableProfile(this.buildGameProfile());
        }
    }

    @Override
    public CraftPlayerProfile clone() {
        CraftPlayerProfile clone = new CraftPlayerProfile(this.getId(), this.getName());
        clone.setProperties(getProperties());
        return clone;
    }

    @Override
    public boolean isComplete() {
        return this.getId() != null && StringUtils.isNotBlank(this.getName());
    }

    @Override
    public @NotNull CompletableFuture<org.bukkit.profile.PlayerProfile> update() {
        return CompletableFuture.supplyAsync(() -> {
            final CraftPlayerProfile clone = clone();
            clone.complete(true);
            return clone;
        }, FusionCoreProfiles.PROFILE_EXECUTOR);
    }

    @Override
    public boolean completeFromCache() {
        return completeFromCache(false, FusionCoreProfiles.isProxyOnlineMode());
    }

    public boolean completeFromCache(boolean onlineMode) {
        return completeFromCache(false, onlineMode);
    }

    public boolean completeFromCache(boolean lookupUUID, boolean onlineMode) {
        MinecraftServer server = ArclightServer.getMinecraftServer();
        String name = profile.getName();
        GameProfileCache userCache = server.getProfileCache();
        if (this.getId() == null) {
            final GameProfile profile;
            if (onlineMode) {
                profile = lookupUUID ? userCache.get(name).orElse(null) : FusionCoreProfiles.getProfileIfCached(name);
            } else {
                // Make an OfflinePlayer using an offline mode UUID since the name has no profile
                profile = new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)), name);
            }
            if (profile != null) {
                // if old has it, assume its newer, so overwrite, else use cached if it was set and ours wasn't
                copyProfileProperties(this.profile, profile);
                this.profile = profile;
                this.emptyUUID = false; // UUID was just retrieved from user cache and profile isn't null (so a completed profile was found)
            }
        }

        if ((profile.getName().isEmpty() || !hasTextures()) && this.getId() != null) {
            Optional<GameProfile> optProfile = userCache.get(this.profile.getId());
            if (optProfile.isPresent()) {
                GameProfile profile = optProfile.get();
                if (this.profile.getName().isEmpty()) {
                    // if old has it, assume its newer, so overwrite, else use cached if it was set and ours wasn't
                    copyProfileProperties(this.profile, profile);
                    this.profile = profile;
                    this.emptyName = false; // Name was just retrieved via the userCache
                } else if (profile != this.profile) {
                    copyProfileProperties(profile, this.profile);
                }
            }
        }
        return this.isComplete();
    }

    public boolean complete(boolean textures) {
        return complete(textures, FusionCoreProfiles.isProxyOnlineMode());
    }

    public boolean complete(boolean textures, boolean onlineMode) {
        if (this.isComplete() && (!textures || hasTextures())) { // Don't do lookup if we already have everything
            return true;
        }

        MinecraftServer server = ArclightServer.getMinecraftServer();
        boolean isCompleteFromCache = this.completeFromCache(true, onlineMode);
        if (onlineMode && (!isCompleteFromCache || (textures && !hasTextures()))) {
            ProfileResult result = io.izzel.arclight.common.mod.util.FusionCoreProfileServices.fetchProfile(server.getSessionService(), asAuthlibCopy(this), true); // Paper's profile fill events (a copy: the answer is copied into this.profile)
            if (result != null && result.profile() != null) {
                copyProfileProperties(result.profile(), this.profile, true);
            }
            if (this.isComplete()) {
                server.getProfileCache().add(this.profile);
            }
        }
        return this.isComplete() && (!onlineMode || !textures || hasTextures());
    }

    private static void copyProfileProperties(GameProfile source, GameProfile target) {
        copyProfileProperties(source, target, false);
    }

    private static void copyProfileProperties(GameProfile source, GameProfile target, boolean clearTarget) {
        if (source == target) {
            throw new IllegalArgumentException("Source and target profiles are the same (" + source + ")");
        }
        PropertyMap sourceProperties = source.getProperties();
        PropertyMap targetProperties = target.getProperties();
        if (clearTarget) targetProperties.clear();
        if (sourceProperties.isEmpty()) {
            return;
        }

        for (Property property : sourceProperties.values()) {
            targetProperties.removeAll(property.name());
            targetProperties.put(property.name(), property);
        }
    }

    private static GameProfile createAuthLibProfile(UUID uniqueId, String name) {
        Preconditions.checkArgument(name == null || name.length() <= 16, "Name cannot be longer than 16 characters");
        Preconditions.checkArgument(name == null || StringUtil.isValidPlayerName(name), "The name of the profile contains invalid characters: %s", name);
        return new GameProfile(
            uniqueId != null ? uniqueId : Util.NIL_UUID,
            name != null ? name : ""
        );
    }

    private static ProfileProperty toBukkit(Property property) {
        return new ProfileProperty(property.name(), property.value(), property.signature());
    }

    public static PlayerProfile asBukkitCopy(GameProfile gameProfile) {
        CraftPlayerProfile profile = new CraftPlayerProfile(gameProfile.getId(), gameProfile.getName());
        copyProfileProperties(gameProfile, profile.profile);
        return profile;
    }

    public static PlayerProfile asBukkitMirror(GameProfile profile) {
        return new CraftPlayerProfile(profile);
    }

    public static Property asAuthlib(ProfileProperty property) {
        return new Property(property.getName(), property.getValue(), property.getSignature());
    }

    public static GameProfile asAuthlibCopy(PlayerProfile profile) {
        CraftPlayerProfile craft = ((CraftPlayerProfile) profile);
        return asAuthlib(craft.clone());
    }

    public static GameProfile asAuthlib(PlayerProfile profile) {
        CraftPlayerProfile craft = ((CraftPlayerProfile) profile);
        return craft.getGameProfile();
    }

    public static ResolvableProfile asResolvableProfileCopy(PlayerProfile profile) {
        return ((CraftPlayerProfile) profile).buildResolvableProfile();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        // Paper's format is Spigot's: uniqueId and name when set, then the properties
        return this.spigotView().serialize();
    }

    public static CraftPlayerProfile deserialize(Map<String, Object> map) {
        // Spigot's parser (same format, validates the unique id and name), then copied
        return new CraftPlayerProfile(org.bukkit.craftbukkit.v.profile.CraftPlayerProfile.deserialize(map).buildResolvableProfile());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final CraftPlayerProfile that = (CraftPlayerProfile) o;
        return this.emptyName == that.emptyName && this.emptyUUID == that.emptyUUID && Objects.equals(this.profile, that.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.emptyName, this.emptyUUID, this.profile);
    }

    @Override
    public String toString() {
        return this.spigotView().toString(); // same text as Paper's
    }

    private class PropertySet extends AbstractSet<ProfileProperty> {

        @Override
        @NotNull
        public Iterator<ProfileProperty> iterator() {
            return new ProfilePropertyIterator(profile.getProperties().values().iterator());
        }

        @Override
        public int size() {
            return profile.getProperties().size();
        }

        @Override
        public boolean add(ProfileProperty property) {
            setProperty(property);
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends ProfileProperty> c) {
            //noinspection unchecked
            setProperties((Collection<ProfileProperty>) c);
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return o instanceof ProfileProperty && profile.getProperties().containsKey(((ProfileProperty) o).getName());
        }

        private class ProfilePropertyIterator implements Iterator<ProfileProperty> {
            private final Iterator<Property> iterator;

            ProfilePropertyIterator(Iterator<Property> iterator) {
                this.iterator = iterator;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ProfileProperty next() {
                return toBukkit(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        }
    }
}
