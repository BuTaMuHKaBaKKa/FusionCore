package io.papermc.paper.advancement;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.translation.Translatable;
import net.kyori.adventure.util.Index;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fusion Core: Paper's {@code AdvancementDisplay}. Plugin calls to Paper's {@code Advancement#getDisplay()} are rewritten
 * by {@code PaperCallTransformer}.
 */
public interface AdvancementDisplay {

    @NotNull Frame frame();

    @NotNull Component title();

    @NotNull Component description();

    @NotNull ItemStack icon();

    boolean doesShowToast();

    boolean doesAnnounceToChat();

    boolean isHidden();

    @Nullable NamespacedKey backgroundPath();

    @NotNull Component displayName();

    enum Frame implements Translatable {

        CHALLENGE("challenge", NamedTextColor.DARK_PURPLE),
        GOAL("goal", NamedTextColor.GREEN),
        TASK("task", NamedTextColor.GREEN);

        public static final Index<String, Frame> NAMES = Index.create(Frame.class, frame -> frame.name);
        private final String name;
        private final TextColor color;

        Frame(final String name, final TextColor color) {
            this.name = name;
            this.color = color;
        }

        public @NotNull TextColor color() {
            return this.color;
        }

        @Override
        public @NotNull String translationKey() {
            return "advancements.toast." + this.name;
        }
    }
}
