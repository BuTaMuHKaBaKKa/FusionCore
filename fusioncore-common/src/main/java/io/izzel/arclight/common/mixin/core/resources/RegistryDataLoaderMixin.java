package io.izzel.arclight.common.mixin.core.resources;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.papermc.paper.registry.PaperRegistryListenerManager;
import io.papermc.paper.registry.data.util.Conversions;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: Paper's registry modification while data registries load (patch 1014). */
@Mixin(RegistryDataLoader.class)
public abstract class RegistryDataLoaderMixin {

    @Unique
    private static final ThreadLocal<Conversions> fusioncore$conversions = new ThreadLocal<>();

    @ModifyExpressionValue(method = "load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryDataLoader;createContext(Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;"))
    private static RegistryOps.RegistryInfoLookup fusioncore$conversions(RegistryOps.RegistryInfoLookup lookup) {
        fusioncore$conversions.set(new Conversions(lookup));
        return lookup;
    }

    @Inject(method = "load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;",
        at = @At("RETURN"))
    private static void fusioncore$clearConversions(CallbackInfoReturnable<?> cir) {
        fusioncore$conversions.remove();
    }

    @WrapOperation(method = "*(Lnet/minecraft/core/WritableRegistry;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/RegistrationInfo;Ljava/lang/Object;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/WritableRegistry;register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;"))
    private static <E> Holder.Reference<E> fusioncore$registerWithListeners(WritableRegistry<E> registry, ResourceKey<E> key, E value, RegistrationInfo info,
                                                                         Operation<Holder.Reference<E>> original) {
        return PaperRegistryListenerManager.INSTANCE.registerWithListeners(registry, key, value, info, fusioncore$conversions.get(),
            (k, v, i) -> original.call(registry, k, v, i));
    }

    @WrapOperation(method = "*(Ljava/util/Map;Lnet/minecraft/resources/RegistryDataLoader$Loader;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;freeze()Lnet/minecraft/core/Registry;"))
    private static Registry<?> fusioncore$runFreezeListeners(Registry<?> registry, Operation<Registry<?>> original) {
        if (registry instanceof WritableRegistry<?> writable) {
            PaperRegistryListenerManager.INSTANCE.runFreezeListeners(writable, fusioncore$conversions.get());
        }
        return original.call(registry);
    }
}
