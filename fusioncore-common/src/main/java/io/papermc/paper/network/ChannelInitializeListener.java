// Fusion Core: Paper 1.21.1 server class (patch 0521 "Add Channel initialization listeners"), checker-framework annotations swapped for JetBrains ones.
package io.papermc.paper.network;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

/**
 * Internal API to register channel initialization listeners.
 * <p>
 * This is not officially supported API and we make no guarantees to the existence or state of this interface.
 */
@FunctionalInterface
public interface ChannelInitializeListener {

    void afterInitChannel(@NotNull Channel channel);
}
