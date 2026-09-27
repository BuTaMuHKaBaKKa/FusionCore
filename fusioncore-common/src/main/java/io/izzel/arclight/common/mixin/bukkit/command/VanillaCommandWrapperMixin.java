package io.izzel.arclight.common.mixin.bukkit.command;

import io.izzel.arclight.common.mod.util.FusionCoreFeedbackSender;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.command.VanillaCommandWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: vanilla commands run by Paper's feedback-forwarding sender (patch 0658) get its own vanilla source. */
@Mixin(value = VanillaCommandWrapper.class, remap = false)
public abstract class VanillaCommandWrapperMixin {

    @Inject(method = "getListener", cancellable = true, at = @At("HEAD"))
    private static void fusioncore$feedbackSender(CommandSender sender, CallbackInfoReturnable<CommandSourceStack> cir) {
        if (sender instanceof FusionCoreFeedbackSender feedback) {
            cir.setReturnValue(feedback.asVanilla());
        }
    }
}
