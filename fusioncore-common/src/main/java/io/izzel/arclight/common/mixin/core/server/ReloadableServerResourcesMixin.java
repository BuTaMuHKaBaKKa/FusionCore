package io.izzel.arclight.common.mixin.core.server;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.papermc.paper.command.brigadier.PaperCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fusion Core: hands the context vanilla's commands are built with to {@link PaperCommands} for Paper's argument types
 * ({@code ArgumentTypes#itemStack()} and the like), as Paper's patch 0965 does in the same constructor.
 */
@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandBuildContext;simple(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/world/flag/FeatureFlagSet;)Lnet/minecraft/commands/CommandBuildContext;"))
    private CommandBuildContext fusioncore$paperBuildContext(CommandBuildContext context) {
        PaperCommands.INSTANCE.setBuildContext(context);
        return context;
    }
}
