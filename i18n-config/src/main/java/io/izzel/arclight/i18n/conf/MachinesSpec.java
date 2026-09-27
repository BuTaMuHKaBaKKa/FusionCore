package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

// Fusion Core: machines of mods that change blocks without a player
@ConfigSerializable
public class MachinesSpec {

    @Setting("owner-events")
    private boolean ownerEvents;

    @Setting("unknown-owner")
    private String unknownOwner;

    @Setting("ignored-mods")
    private List<String> ignoredMods;

    public boolean isOwnerEvents() {
        return ownerEvents;
    }

    /** {@code allow}, {@code deny} or {@code mod}; anything else is {@code allow} */
    public String getUnknownOwner() {
        return unknownOwner == null ? "allow" : unknownOwner;
    }

    public boolean isIgnoredMod(String namespace) {
        return ignoredMods != null && ignoredMods.contains(namespace);
    }
}
