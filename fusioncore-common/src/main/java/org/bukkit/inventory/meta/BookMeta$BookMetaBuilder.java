package org.bukkit.inventory.meta;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/** Fusion Core: Paper's {@code BookMeta.BookMetaBuilder}, compiled as a top-level class under its binary name. */
@SuppressWarnings("checkstyle:TypeName")
public interface BookMeta$BookMetaBuilder extends Book.Builder {

    @Override
    @NotNull BookMeta$BookMetaBuilder title(@Nullable Component title);

    @Override
    @NotNull BookMeta$BookMetaBuilder author(@Nullable Component author);

    @Override
    @NotNull BookMeta$BookMetaBuilder addPage(@NotNull Component page);

    @Override
    @NotNull BookMeta$BookMetaBuilder pages(@NotNull Component @NotNull ... pages);

    @Override
    @NotNull BookMeta$BookMetaBuilder pages(@NotNull Collection<Component> pages);

    // Paper declares BookMeta build(); javac cannot see that a BookMeta is a Book, so the inherited
    // Book build() stays, and PaperCallTransformer rewrites calls to the BookMeta variant.
}
