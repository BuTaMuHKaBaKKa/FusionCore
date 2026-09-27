package io.izzel.arclight.common.mixin.core.tags;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.izzel.arclight.common.bridge.core.tags.TagLoaderBridge;
import io.papermc.paper.tag.PaperTagListenerManager;
import io.papermc.paper.tag.TagEventConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Fusion Core: Paper's tag lifecycle events in {@code TagLoader#build} (patch 1053). */
@Mixin(TagLoader.class)
public abstract class TagLoaderMixin<T> implements TagLoaderBridge {

    @Unique
    private TagEventConfig<?, ?> fusioncore$eventConfig;

    @Override
    public void fusioncore$setEventConfig(TagEventConfig<?, ?> config) {
        this.fusioncore$eventConfig = config;
    }

    @ModifyVariable(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("HEAD"), argsOnly = true)
    private Map<ResourceLocation, List<TagLoader.EntryWithSource>> fusioncore$preFlatten(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tags) {
        return PaperTagListenerManager.INSTANCE.firePreFlattenEvent(tags, this.fusioncore$eventConfig);
    }

    @SuppressWarnings("unchecked")
    @ModifyReturnValue(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("RETURN"))
    private Map<ResourceLocation, Collection<T>> fusioncore$postFlatten(Map<ResourceLocation, Collection<T>> tags) {
        return PaperTagListenerManager.INSTANCE.firePostFlattenEvent(tags, (TagEventConfig<T, Object>) this.fusioncore$eventConfig);
    }
}
