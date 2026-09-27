package io.izzel.arclight.common.mixin.bukkit;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.papermc.paper.CraftGameEventTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: Paper's game event tags in {@code Server#getTag}/{@code getTags}. */
@Mixin(value = CraftServer.class, remap = false)
public abstract class CraftServerTagsMixin {

    private static final String FUSIONCORE$GAME_EVENTS = "game_events";

    @SuppressWarnings("unchecked")
    @Inject(method = "getTag", cancellable = true, at = @At("HEAD"))
    private <T extends Keyed> void fusioncore$gameEventTag(String registry, NamespacedKey tag, Class<T> clazz, CallbackInfoReturnable<org.bukkit.Tag<T>> cir) {
        if (FUSIONCORE$GAME_EVENTS.equals(registry) && tag != null) {
            Preconditions.checkArgument(clazz == org.bukkit.GameEvent.class, "Game Event namespace must have GameEvent type");
            TagKey<net.minecraft.world.level.gameevent.GameEvent> gameEventTagKey = TagKey.create(Registries.GAME_EVENT, CraftNamespacedKey.toMinecraft(tag));
            cir.setReturnValue(BuiltInRegistries.GAME_EVENT.getTag(gameEventTagKey).isPresent()
                ? (org.bukkit.Tag<T>) new CraftGameEventTag(BuiltInRegistries.GAME_EVENT, gameEventTagKey) : null);
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "getTags", cancellable = true, at = @At("HEAD"))
    private <T extends Keyed> void fusioncore$gameEventTags(String registry, Class<T> clazz, CallbackInfoReturnable<Iterable<org.bukkit.Tag<T>>> cir) {
        if (FUSIONCORE$GAME_EVENTS.equals(registry)) {
            Preconditions.checkArgument(clazz == org.bukkit.GameEvent.class);
            net.minecraft.core.Registry<net.minecraft.world.level.gameevent.GameEvent> gameEvents = BuiltInRegistries.GAME_EVENT;
            cir.setReturnValue(gameEvents.getTags().map(pair -> (org.bukkit.Tag<T>) new CraftGameEventTag(gameEvents, pair.getFirst())).collect(ImmutableList.toImmutableList()));
        }
    }
}
