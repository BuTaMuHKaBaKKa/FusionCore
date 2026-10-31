package io.izzel.arclight.common.mixin.core.world.level.chunk;

import io.izzel.arclight.common.bridge.core.world.level.chunk.LevelChunkSectionBridge;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelChunkSection.class)
public class LevelChunkSectionMixin implements LevelChunkSectionBridge {

    // Fusion Core: Paper's Anti-Xray, the block states (not the biomes) of this section are being written to a chunk packet
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "write", at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/PalettedContainer;write(Lnet/minecraft/network/FriendlyByteBuf;)V"))
    private void fusioncore$statesWriting(net.minecraft.world.level.chunk.PalettedContainer<?> states, net.minecraft.network.FriendlyByteBuf buf,
                                          com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        if (io.izzel.arclight.common.mod.util.FusionCoreAntiXray.current() == null) {
            original.call(states, buf);
            return;
        }
        io.izzel.arclight.common.mod.util.FusionCoreAntiXray.statesWriting(true);
        try {
            original.call(states, buf);
        } finally {
            io.izzel.arclight.common.mod.util.FusionCoreAntiXray.statesWriting(false);
        }
    }

    @Shadow private PalettedContainerRO<Holder<Biome>> biomes;

    public void setBiome(int i, int j, int k, Holder<net.minecraft.world.level.biome.Biome> biome) {
        ((PalettedContainer<Holder<Biome>>) this.biomes).set(i, j, k, biome);
    }

    @Override
    public void bridge$setBiome(int x, int y, int z, Holder<Biome> biome) {
        this.setBiome(x, y, z, biome);
    }
}
