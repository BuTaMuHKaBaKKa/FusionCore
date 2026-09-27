package io.papermc.paper.datapack;

import com.google.common.collect.Collections2;
import io.izzel.arclight.common.mod.server.ArclightServer;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: Paper's {@code PaperDatapackManager} (Paper patch 0529), over the server's pack repository. */
public class PaperDatapackManager implements DatapackManager {

    public static final PaperDatapackManager INSTANCE = new PaperDatapackManager();

    private PackRepository repository() {
        return ArclightServer.getMinecraftServer().getPackRepository();
    }

    @Override
    public void refreshPacks() {
        this.repository().reload();
    }

    @Override
    public @Nullable Datapack getPack(@NotNull String name) {
        PackRepository repository = this.repository();
        Pack pack = repository.getPack(name);
        if (pack == null) {
            return null;
        }
        return new PaperDatapack(pack, repository.getSelectedPacks().contains(pack));
    }

    @Override
    public @NotNull Collection<Datapack> getPacks() {
        PackRepository repository = this.repository();
        Collection<Pack> enabledPacks = repository.getSelectedPacks();
        return this.transformPacks(repository.getAvailablePacks(), enabledPacks::contains);
    }

    @Override
    public @NotNull Collection<Datapack> getEnabledPacks() {
        return this.transformPacks(this.repository().getSelectedPacks(), pack -> true);
    }

    private Collection<Datapack> transformPacks(Collection<Pack> packs, Predicate<Pack> enabled) {
        return Collections.unmodifiableCollection(Collections2.transform(packs, pack -> new PaperDatapack(pack, enabled.test(pack))));
    }
}
