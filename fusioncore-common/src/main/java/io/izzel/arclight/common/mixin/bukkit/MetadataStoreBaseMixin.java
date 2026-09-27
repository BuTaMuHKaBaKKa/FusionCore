package io.izzel.arclight.common.mixin.bukkit;

import com.google.common.base.Preconditions;
import org.bukkit.metadata.MetadataStoreBase;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Map;

/**
 * Fusion Core: Paper's {@code MetadataStoreBase#removeAll(Plugin)} (patch 0034). Unlike Paper's, it is
 * {@code synchronized}, as are Spigot's other methods of the store that touch the same map.
 */
@Mixin(value = MetadataStoreBase.class, remap = false)
public abstract class MetadataStoreBaseMixin {

    @Shadow private Map<String, Map<Plugin, MetadataValue>> metadataMap;

    /**
     * Removes all metadata in the metadata store that originates from the
     * given plugin.
     *
     * @param owningPlugin the plugin requesting the invalidation.
     * @throws IllegalArgumentException If plugin is null
     */
    public synchronized void removeAll(Plugin owningPlugin) {
        Preconditions.checkNotNull(owningPlugin, "Plugin cannot be null");
        for (Iterator<Map<Plugin, MetadataValue>> iterator = this.metadataMap.values().iterator(); iterator.hasNext(); ) {
            Map<Plugin, MetadataValue> values = iterator.next();
            values.remove(owningPlugin);
            if (values.isEmpty()) {
                iterator.remove();
            }
        }
    }
}
