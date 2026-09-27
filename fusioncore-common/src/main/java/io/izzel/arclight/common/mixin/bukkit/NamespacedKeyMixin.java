package io.izzel.arclight.common.mixin.bukkit;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Fusion Core: {@link NamespacedKey} is an Adventure {@link Key} and a {@link com.destroystokyo.paper.Namespaced}, as in Paper,
 * with Paper's equality and hash.
 */
@Mixin(value = NamespacedKey.class, remap = false)
public abstract class NamespacedKeyMixin implements Key, com.destroystokyo.paper.Namespaced {

    @Override @Shadow public abstract @NotNull String getNamespace();

    @Override @Shadow public abstract @NotNull String getKey();

    @Override
    public @NotNull String namespace() {
        return this.getNamespace();
    }

    @Override
    public @NotNull String value() {
        return this.getKey();
    }

    @Override
    public @NotNull String asString() {
        return this.getNamespace() + ':' + this.getKey();
    }

    /**
     * @author Fusion Core
     * @reason Paper's hash, the same as Adventure's {@code Key} implementation
     */
    @Overwrite
    public int hashCode() {
        int result = this.getNamespace().hashCode();
        result = (31 * result) + this.getKey().hashCode();
        return result;
    }

    /**
     * @author Fusion Core
     * @reason Paper's equality: any Adventure {@code Key} with the same namespace and value
     */
    @Overwrite
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Key key)) return false;
        return this.getNamespace().equals(key.namespace()) && this.getKey().equals(key.value());
    }
}
