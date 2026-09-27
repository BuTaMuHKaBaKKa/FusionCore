package io.izzel.arclight.common.mixin.core.server;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.izzel.arclight.common.mod.util.FusionCoreProfileServices;
import net.minecraft.server.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper's profile events through the server's session service and profile repository, see {@link FusionCoreProfileServices}. */
@Mixin(Services.class)
public abstract class ServicesMixin {

    @ModifyExpressionValue(method = "create", at = @At(value = "INVOKE", remap = false, target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;createMinecraftSessionService()Lcom/mojang/authlib/minecraft/MinecraftSessionService;"))
    private static MinecraftSessionService fusioncore$paperSessionService(MinecraftSessionService service) {
        return service instanceof FusionCoreProfileServices.SessionService ? service : new FusionCoreProfileServices.SessionService(service);
    }

    @ModifyExpressionValue(method = "create", at = @At(value = "INVOKE", remap = false, target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;createProfileRepository()Lcom/mojang/authlib/GameProfileRepository;"))
    private static GameProfileRepository fusioncore$paperProfileRepository(GameProfileRepository repository) {
        return repository instanceof FusionCoreProfileServices.ProfileRepository ? repository : new FusionCoreProfileServices.ProfileRepository(repository);
    }
}
