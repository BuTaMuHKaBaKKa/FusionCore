package io.izzel.arclight.common.mixin.core.server.players;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.mojang.authlib.GameProfile;
import io.papermc.paper.event.server.WhitelistStateUpdateEvent;
import io.papermc.paper.event.server.WhitelistStateUpdateEvent.WhitelistStatus;
import net.minecraft.server.players.UserWhiteList;
import io.izzel.arclight.common.bridge.core.server.players.StoredUserListBridge;

import java.util.Collection;
import java.util.Map;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.StoredUserList;

@Mixin(StoredUserList.class)
public class StoredUserListMixin<K, V extends StoredUserEntry<K>> implements StoredUserListBridge<V> {

    // @formatter:off
    @Shadow @Final private Map<String, V> map;
    // @formatter:on

    public Collection<V> getValues() {
        return this.map.values();
    }

    @Override
    public Collection<V> bridge$getValues() {
        return getValues();
    }

    // ---- Fusion Core: Paper's WhitelistStateUpdateEvent (patch 0841): adding to or removing from the whitelist, cancellable.
    // Paper overrides add/remove in UserWhiteList; they are inherited from here, so the check is on the list's class.

    @Inject(method = "add", cancellable = true, at = @At("HEAD"))
    private void fusioncore$whitelistAdd(V entry, CallbackInfo ci) {
        if ((Object) this instanceof UserWhiteList && entry.getUser() instanceof GameProfile profile
            && !io.izzel.arclight.common.mod.util.FusionCoreEvents.call(new WhitelistStateUpdateEvent(CraftPlayerProfile.asBukkitCopy(profile), WhitelistStatus.ADDED))) {
            ci.cancel();
        }
    }

    @Inject(method = "remove(Ljava/lang/Object;)V", cancellable = true, at = @At("HEAD"))
    private void fusioncore$whitelistRemove(K user, CallbackInfo ci) {
        if ((Object) this instanceof UserWhiteList && user instanceof GameProfile profile
            && !io.izzel.arclight.common.mod.util.FusionCoreEvents.call(new WhitelistStateUpdateEvent(CraftPlayerProfile.asBukkitCopy(profile), WhitelistStatus.REMOVED))) {
            ci.cancel();
        }
    }
}
