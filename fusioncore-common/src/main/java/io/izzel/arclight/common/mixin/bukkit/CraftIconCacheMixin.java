package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.craftbukkit.v.util.CraftIconCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Fusion Core: Paper's {@code CachedServerIcon#getData()} (see {@link CachedServerIconMixin}): the icon
 * as a {@code data:image/png;base64,} URI, or {@code null} for no icon.
 */
@Mixin(value = CraftIconCache.class, remap = false)
public abstract class CraftIconCacheMixin {

    @Shadow @Final public byte[] value;

    public String getData() {
        if (this.value == null) {
            return null;
        }
        return "data:image/png;base64," + new String(Base64.getEncoder().encode(this.value), StandardCharsets.UTF_8);
    }
}
