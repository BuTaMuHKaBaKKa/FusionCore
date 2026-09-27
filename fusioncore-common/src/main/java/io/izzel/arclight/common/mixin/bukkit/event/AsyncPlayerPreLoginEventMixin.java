package io.izzel.arclight.common.mixin.bukkit.event;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.izzel.arclight.common.bridge.bukkit.AsyncPlayerPreLoginEventBridge;
import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import io.izzel.arclight.common.mod.util.FusionCoreProfiles;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Fusion Core: Paper's kick message component, profile, raw address and hostname (patches 0006, 0087).
 * The profile a listener sets is the one the player logs in with.
 */
@Mixin(value = AsyncPlayerPreLoginEvent.class, remap = false)
public abstract class AsyncPlayerPreLoginEventMixin implements AsyncPlayerPreLoginEventBridge {

    @Shadow public abstract String getKickMessage();

    @Shadow public abstract void setKickMessage(String value);

    @Unique private PlayerProfile fusioncore$profile;
    @Unique private InetAddress fusioncore$rawAddress;
    @Unique private String fusioncore$hostname;

    @Inject(method = "<init>(Ljava/lang/String;Ljava/net/InetAddress;Ljava/util/UUID;Z)V", at = @At("RETURN"))
    private void fusioncore$init(String name, InetAddress ipAddress, UUID uniqueId, boolean transferred, CallbackInfo ci) {
        this.fusioncore$profile = FusionCoreProfiles.createProfile(uniqueId, name);
        this.fusioncore$rawAddress = ipAddress;
        this.fusioncore$hostname = "";
    }

    @Override
    public void fusioncore$setPaperFields(PlayerProfile profile, InetAddress rawAddress, String hostname) {
        this.fusioncore$profile = profile;
        this.fusioncore$rawAddress = rawAddress;
        this.fusioncore$hostname = hostname;
    }

    /**
     * @author Fusion Core
     * @reason Paper: the name is the profile's
     */
    @Overwrite
    public String getName() {
        return this.fusioncore$profile.getName();
    }

    /**
     * @author Fusion Core
     * @reason Paper: the unique id is the profile's
     */
    @Overwrite
    public UUID getUniqueId() {
        return this.fusioncore$profile.getId();
    }

    public PlayerProfile getPlayerProfile() {
        return this.fusioncore$profile;
    }

    @Override
    public PlayerProfile fusioncore$getPlayerProfile() {
        return this.fusioncore$profile;
    }

    public void setPlayerProfile(PlayerProfile profile) {
        this.fusioncore$profile = profile;
    }

    public InetAddress getRawAddress() {
        return this.fusioncore$rawAddress;
    }

    public String getHostname() {
        return this.fusioncore$hostname;
    }

    public Component kickMessage() {
        return FusionCoreAdventure.fromLegacy(this.getKickMessage());
    }

    public void kickMessage(Component kickMessage) {
        this.setKickMessage(FusionCoreAdventure.toLegacyRgb(kickMessage == null ? Component.empty() : kickMessage));
    }

    @Shadow public abstract void disallow(AsyncPlayerPreLoginEvent.Result result, String message);

    public void disallow(AsyncPlayerPreLoginEvent.Result result, Component message) {
        this.disallow(result, FusionCoreAdventure.toLegacyRgb(message == null ? Component.empty() : message));
    }

    @SuppressWarnings("deprecation")
    public void disallow(org.bukkit.event.player.PlayerPreLoginEvent.Result result, Component message) {
        this.disallow(result == null ? null : AsyncPlayerPreLoginEvent.Result.valueOf(result.name()), message);
    }
}
