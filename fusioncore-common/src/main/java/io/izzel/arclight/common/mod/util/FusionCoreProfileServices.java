package io.izzel.arclight.common.mod.util;

import com.destroystokyo.paper.event.profile.FillProfileEvent;
import com.destroystokyo.paper.event.profile.LookupProfileEvent;
import com.destroystokyo.paper.event.profile.PreFillProfileEvent;
import com.destroystokyo.paper.event.profile.PreLookupProfileEvent;
import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ProfileResult;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Fusion Core: Paper's profile lookup and fill events (patches 0139, 0167), via wrappers around the server's services. */
public final class FusionCoreProfileServices {

    private FusionCoreProfileServices() {
    }

    /** Paper's {@code PaperMinecraftSessionService#fetchProfile(GameProfile, boolean)}, or a plain lookup for a foreign service. */
    public static @Nullable ProfileResult fetchProfile(MinecraftSessionService service, GameProfile profile, boolean requireSecure) {
        if (service instanceof SessionService paper) {
            return paper.fetchProfile(profile, requireSecure);
        }
        return service.fetchProfile(profile.getId(), requireSecure);
    }

    public static final class SessionService implements MinecraftSessionService {

        private final MinecraftSessionService delegate;

        public SessionService(MinecraftSessionService delegate) {
            this.delegate = delegate;
        }

        public MinecraftSessionService delegate() {
            return this.delegate;
        }

        public @Nullable ProfileResult fetchProfile(GameProfile profile, boolean requireSecure) {
            CraftPlayerProfile playerProfile = (CraftPlayerProfile) CraftPlayerProfile.asBukkitMirror(profile);
            io.izzel.arclight.common.mod.util.FusionCoreEvents.call(new PreFillProfileEvent(playerProfile));
            profile = playerProfile.getGameProfile();
            if (profile.getProperties().containsKey("textures")) {
                return new ProfileResult(profile, Collections.emptySet());
            }
            ProfileResult result = this.delegate.fetchProfile(profile.getId(), requireSecure);
            if (result != null) {
                io.izzel.arclight.common.mod.util.FusionCoreEvents.call(new FillProfileEvent(CraftPlayerProfile.asBukkitMirror(result.profile())));
            }
            return result;
        }

        @Override
        public @Nullable ProfileResult fetchProfile(UUID profileId, boolean requireSecure) {
            return this.fetchProfile(new GameProfile(profileId, cachedName(profileId)), requireSecure);
        }

        private static String cachedName(UUID id) {
            MinecraftServer server = ArclightServer.getMinecraftServer();
            GameProfileCache cache = server == null ? null : server.getProfileCache();
            return cache == null ? "" : cache.get(id).map(GameProfile::getName).orElse("");
        }

        @Override
        public void joinServer(UUID profileId, String authenticationToken, String serverId) throws AuthenticationException {
            this.delegate.joinServer(profileId, authenticationToken, serverId);
        }

        @Override
        public @Nullable ProfileResult hasJoinedServer(String profileName, String serverId, @Nullable InetAddress address) throws AuthenticationUnavailableException {
            return this.delegate.hasJoinedServer(profileName, serverId, address);
        }

        @Override
        public @Nullable Property getPackedTextures(GameProfile profile) {
            return this.delegate.getPackedTextures(profile);
        }

        @Override
        public MinecraftProfileTextures unpackTextures(Property packedTextures) {
            return this.delegate.unpackTextures(packedTextures);
        }

        @Override
        public MinecraftProfileTextures getTextures(GameProfile profile) {
            return this.delegate.getTextures(profile);
        }

        @Override
        public String getSecurePropertyValue(Property property) throws InsecurePublicKeyException {
            return this.delegate.getSecurePropertyValue(property);
        }
    }

    public static final class ProfileRepository implements GameProfileRepository {

        private final GameProfileRepository delegate;

        public ProfileRepository(GameProfileRepository delegate) {
            this.delegate = delegate;
        }

        public GameProfileRepository delegate() {
            return this.delegate;
        }

        @Override
        public void findProfilesByNames(String[] names, ProfileLookupCallback callback) {
            List<String> unfound = new ArrayList<>();
            for (String name : names) {
                PreLookupProfileEvent event = new PreLookupProfileEvent(name);
                io.izzel.arclight.common.mod.util.FusionCoreEvents.call(event);
                if (event.getUUID() != null) {
                    // the plugin gave the UUID (and maybe properties): no request
                    GameProfile profile = new GameProfile(event.getUUID(), name);
                    for (ProfileProperty property : event.getProfileProperties()) {
                        profile.getProperties().put(property.getName(), CraftPlayerProfile.asAuthlib(property));
                    }
                    callback.onProfileLookupSucceeded(profile);
                } else {
                    unfound.add(name);
                }
            }
            if (!unfound.isEmpty()) {
                this.delegate.findProfilesByNames(unfound.toArray(new String[0]), new ProfileLookupCallback() {
                    @Override
                    public void onProfileLookupSucceeded(GameProfile profile) {
                        io.izzel.arclight.common.mod.util.FusionCoreEvents.call(new LookupProfileEvent(CraftPlayerProfile.asBukkitMirror(profile)));
                        callback.onProfileLookupSucceeded(profile);
                    }

                    @Override
                    public void onProfileLookupFailed(String profileName, Exception exception) {
                        callback.onProfileLookupFailed(profileName, exception);
                    }
                });
            }
        }
    }
}
