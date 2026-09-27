package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.persistence.PersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: {@link PersistentDataContainer} as Paper's {@code PersistentDataContainerView}, with byte serialization. */
@Mixin(value = PersistentDataContainer.class, remap = false)
public interface PersistentDataContainerMixin extends io.papermc.paper.persistence.PersistentDataContainerView {

    @Override
    default byte[] serializeToBytes() throws java.io.IOException {
        return io.izzel.arclight.common.mod.util.FusionCoreItems.serializeContainer((PersistentDataContainer) this);
    }

    default void readFromBytes(byte[] bytes, boolean clear) throws java.io.IOException {
        io.izzel.arclight.common.mod.util.FusionCoreItems.readContainer((PersistentDataContainer) this, bytes, clear);
    }

    default void readFromBytes(byte[] bytes) throws java.io.IOException {
        this.readFromBytes(bytes, true);
    }
}
