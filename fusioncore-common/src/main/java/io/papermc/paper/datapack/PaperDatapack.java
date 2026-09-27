package io.papermc.paper.datapack;

import io.izzel.arclight.common.mod.server.ArclightServer;
import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.bukkit.FeatureFlag;
import org.jetbrains.annotations.NotNull;

/** Fusion Core: Paper's {@code PaperDatapack} (patch 0529). Enabling or disabling reloads data packs. */
public class PaperDatapack implements Datapack {

    private static final Map<PackSource, DatapackSource> PACK_SOURCES = new ConcurrentHashMap<>();

    static {
        PACK_SOURCES.put(PackSource.DEFAULT, DatapackSource.DEFAULT);
        PACK_SOURCES.put(PackSource.BUILT_IN, DatapackSource.BUILT_IN);
        PACK_SOURCES.put(PackSource.FEATURE, DatapackSource.FEATURE);
        PACK_SOURCES.put(PackSource.WORLD, DatapackSource.WORLD);
        PACK_SOURCES.put(PackSource.SERVER, DatapackSource.SERVER);
    }

    private final Pack pack;
    private final boolean enabled;

    PaperDatapack(Pack pack, boolean enabled) {
        this.pack = pack;
        this.enabled = enabled;
    }

    @Override
    public @NotNull String getName() {
        return this.pack.getId();
    }

    @Override
    public @NotNull Component getTitle() {
        return FusionCoreAdventure.asAdventure(this.pack.getTitle());
    }

    @Override
    public @NotNull Component getDescription() {
        return FusionCoreAdventure.asAdventure(this.pack.getDescription());
    }

    @Override
    public boolean isRequired() {
        return this.pack.isRequired();
    }

    @Override
    public @NotNull Compatibility getCompatibility() {
        return Datapack.Compatibility.valueOf(this.pack.getCompatibility().name());
    }

    @Override
    public @NotNull Set<FeatureFlag> getRequiredFeatures() {
        return io.izzel.arclight.common.mod.util.FusionCoreFeatureFlags.fromNms(this.pack.getRequestedFeatures()); // Fusion Core: canonical flags
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        MinecraftServer server = ArclightServer.getMinecraftServer();
        List<Pack> enabledPacks = new ArrayList<>(server.getPackRepository().getSelectedPacks());
        Pack packToChange = server.getPackRepository().getPack(this.getName());
        if (packToChange == null) {
            throw new IllegalStateException("Cannot toggle state of pack that doesn't exist: " + this.getName());
        }
        if (enabled == enabledPacks.contains(packToChange)) {
            return;
        }
        if (enabled) {
            packToChange.getDefaultPosition().insert(enabledPacks, packToChange, Pack::selectionConfig, false); // modeled off the default /datapack enable logic
        } else {
            enabledPacks.remove(packToChange);
        }
        server.reloadResources(enabledPacks.stream().map(Pack::getId).toList());
    }

    @Override
    public @NotNull DatapackSource getSource() {
        return PACK_SOURCES.computeIfAbsent(this.pack.location().source(), source -> new DatapackSourceImpl(source.toString()));
    }

    @Override
    public @NotNull Component computeDisplayName() {
        return FusionCoreAdventure.asAdventure(this.pack.getChatLink(this.enabled));
    }
}
