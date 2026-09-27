package io.izzel.arclight.common.mixin.core.tags;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.izzel.arclight.common.bridge.core.tags.TagLoaderBridge;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.tag.PaperTagListenerManager;
import io.papermc.paper.tag.TagEventConfig;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.atomic.AtomicBoolean;

/** Fusion Core: gives each registry's tag loader Paper's event config (INITIAL on first load, RELOAD afterwards). */
@Mixin(TagManager.class)
public abstract class TagManagerMixin {

    @Unique
    private static final AtomicBoolean fusioncore$loaded = new AtomicBoolean();

    @ModifyExpressionValue(method = "createLoader", at = @At(value = "NEW", target = "(Ljava/util/function/Function;Ljava/lang/String;)Lnet/minecraft/tags/TagLoader;"))
    private <T> TagLoader<?> fusioncore$eventConfig(TagLoader<?> loader, @Local(argsOnly = true) RegistryAccess.RegistryEntry<T> entry) {
        ReloadableRegistrarEvent.Cause cause = fusioncore$loaded.get() ? ReloadableRegistrarEvent.Cause.RELOAD : ReloadableRegistrarEvent.Cause.INITIAL;
        TagEventConfig<?, ?> config = PaperTagListenerManager.INSTANCE.createEventConfig(entry.value(), cause);
        if (config != null) {
            ((TagLoaderBridge) loader).fusioncore$setEventConfig(config);
        }
        return loader;
    }

    @ModifyExpressionValue(method = "reload", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;allOf([Ljava/util/concurrent/CompletableFuture;)Ljava/util/concurrent/CompletableFuture;"))
    private java.util.concurrent.CompletableFuture<Void> fusioncore$markLoaded(java.util.concurrent.CompletableFuture<Void> future) {
        fusioncore$loaded.set(true); // every loader of this reload has its cause already
        return future;
    }
}
