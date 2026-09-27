package io.izzel.arclight.common.mixin.bukkit.event;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.server.TabCompleteEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Fusion Core: Paper's {@code TabCompleteEvent#isCommand}/{@code getLocation}; completions are a mutable copy. */
@Mixin(value = TabCompleteEvent.class, remap = false)
public abstract class TabCompleteEventMixin implements io.izzel.arclight.common.bridge.bukkit.TabCompleteEventBridge {

    @Shadow private List<String> completions;

    @Unique private boolean fusioncore$isCommand;
    @Unique private Location fusioncore$location;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fusioncore$init(CommandSender sender, String buffer, List<String> completions, CallbackInfo ci) {
        this.fusioncore$isCommand = sender instanceof ConsoleCommandSender || buffer.startsWith("/");
        this.completions = new java.util.ArrayList<>(completions);
    }

    @Inject(method = "setCompletions", at = @At("RETURN"))
    private void fusioncore$mutable(List<String> completions, CallbackInfo ci) {
        this.completions = new java.util.ArrayList<>(completions);
    }

    public boolean isCommand() {
        return this.fusioncore$isCommand;
    }

    public Location getLocation() {
        return this.fusioncore$location != null ? this.fusioncore$location.clone() : null;
    }

    @Override
    public void bridge$init(boolean isCommand, Location location) {
        this.fusioncore$isCommand = isCommand;
        this.fusioncore$location = location;
    }
}
