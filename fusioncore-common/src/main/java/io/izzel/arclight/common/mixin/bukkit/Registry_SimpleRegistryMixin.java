package io.izzel.arclight.common.mixin.bukkit;

import com.google.common.collect.ImmutableMap;
import io.izzel.arclight.common.bridge.bukkit.SimpleRegistryBridge;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Predicate;

@Mixin(value = Registry.SimpleRegistry.class, remap = false)
public class Registry_SimpleRegistryMixin<T extends Enum<T> & Keyed> implements SimpleRegistryBridge {

    @Shadow @Final @Mutable private Map<NamespacedKey, T> map;

    private Runnable arclight$reloadCallback;

    @Inject(method = "<init>(Ljava/lang/Class;Ljava/util/function/Predicate;)V", at = @At("RETURN"))
    private void arclight$init(Class<T> type, Predicate<T> predicate, CallbackInfo ci) {
        this.arclight$reloadCallback = () -> {
            ImmutableMap.Builder<NamespacedKey, T> builder = ImmutableMap.builder();

            for (T entry : type.getEnumConstants()) {
                if (predicate.test(entry)) {
                    builder.put(entry.getKey(), entry);
                }
            }

            map = builder.build();
        };
    }

    @Override
    public void bridge$reload() {
        this.arclight$reloadCallback.run();
    }

    /** Paper ("improve Registry"): SimpleRegistry's own {@code getKey(T)}, erased to {@code getKey(Enum)}. */
    public NamespacedKey getKey(T value) {
        return value.getKey();
    }

    /** Paper ("RegistrySet API"): tags of a simple registry come from {@code UnsafeValues#getTag}. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean hasTag(io.papermc.paper.registry.tag.TagKey<T> key) {
        return io.izzel.arclight.common.mod.util.FusionCoreRegistries.tag((io.papermc.paper.registry.tag.TagKey) key) != null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public io.papermc.paper.registry.tag.Tag<T> getTag(io.papermc.paper.registry.tag.TagKey<T> key) {
        final io.papermc.paper.registry.tag.Tag<T> tag = io.izzel.arclight.common.mod.util.FusionCoreRegistries.tag((io.papermc.paper.registry.tag.TagKey) key);
        if (tag == null) {
            throw new java.util.NoSuchElementException("No tag " + key + " found");
        }
        return tag;
    }
}
