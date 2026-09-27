package io.izzel.arclight.common.mixin.core.server;

import io.izzel.arclight.common.mod.util.ArclightCaptures;
import net.minecraft.server.WorldLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldLoader.class)
public class WorldLoaderMixin {

    // Fusion Core: Paper plugins are bootstrapped before the data registries load, as Paper does before it starts loading
    @Inject(method = "load", at = @At("HEAD"))
    private static void fusioncore$enterBootstrappers(CallbackInfoReturnable<?> cir) {
        io.izzel.arclight.common.mod.util.FusionCorePaperPlugins.enterBootstrappers();
    }

    @ModifyArg(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/WorldLoader$WorldDataSupplier;get(Lnet/minecraft/server/WorldLoader$DataLoadContext;)Lnet/minecraft/server/WorldLoader$DataLoadOutput;"))
    private static WorldLoader.DataLoadContext arclight$captureContext(WorldLoader.DataLoadContext context) {
        ArclightCaptures.captureDataLoadContext(context);
        return context;
    }
}
