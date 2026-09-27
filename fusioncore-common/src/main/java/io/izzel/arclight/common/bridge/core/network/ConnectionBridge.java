package io.izzel.arclight.common.bridge.core.network;

import com.mojang.authlib.properties.Property;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

public interface ConnectionBridge {

    UUID bridge$getSpoofedUUID();

    void bridge$setSpoofedUUID(UUID spoofedUUID);

    Property[] bridge$getSpoofedProfile();

    void bridge$setSpoofedProfile(Property[] spoofedProfile);

    String bridge$getHostname();

    void bridge$setHostname(String hostname);

    // Fusion Core: from the handshake, for Paper's NetworkClient
    int bridge$getProtocolVersion();

    InetSocketAddress bridge$getVirtualHost();

    void bridge$setClientIntention(int protocolVersion, InetSocketAddress virtualHost);
}
