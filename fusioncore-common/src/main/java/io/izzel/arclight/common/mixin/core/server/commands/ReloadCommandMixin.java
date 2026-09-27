package io.izzel.arclight.common.mixin.core.server.commands;

import io.izzel.arclight.common.mod.mixins.annotation.TransformAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(ReloadCommand.class)
public abstract class ReloadCommandMixin {

    // @formatter:off
    @Shadow private static Collection<String> discoverNewPacks(PackRepository p_241058_0_, WorldData p_241058_1_, Collection<String> p_241058_2_) { return null; }
    // @formatter:on

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static void reload(MinecraftServer minecraftserver) {
        PackRepository resourcePackList = minecraftserver.getPackRepository();
        WorldData configuration = minecraftserver.getWorldData();
        Collection<String> collection = resourcePackList.getSelectedIds();
        Collection<String> collection2 = discoverNewPacks(resourcePackList, configuration, collection);
        minecraftserver.reloadResources(collection2);
    }

    // Fusion Core: /reload is Paper's ServerResourcesReloadedEvent cause COMMAND
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "reloadPacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;reloadResources(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;"))
    private static java.util.concurrent.CompletableFuture<Void> fusioncore$reloadByCommand(net.minecraft.server.MinecraftServer server, java.util.Collection<String> packs,
                                                                                         com.llamalad7.mixinextras.injector.wrapoperation.Operation<java.util.concurrent.CompletableFuture<Void>> original) {
        io.izzel.arclight.common.mod.util.FusionCoreEvents.reloadCause(io.papermc.paper.event.server.ServerResourcesReloadedEvent.Cause.COMMAND);
        return original.call(server, packs);
    }
}
