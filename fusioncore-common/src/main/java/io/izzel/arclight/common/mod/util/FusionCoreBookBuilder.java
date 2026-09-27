package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.bukkit.BookMetaBridge;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta$BookMetaBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Fusion Core: Paper's {@code CraftMetaBook.CraftMetaBookBuilder} and {@code CraftMetaBookSigned.CraftMetaBookSignedBuilder}
 * (Paper patch 0010 "Adventure") in one class. A writable book ignores title and author, as in Paper.
 */
public final class FusionCoreBookBuilder implements BookMeta$BookMetaBuilder {

    /** Paper's {@code LEGACY_DOWNSAMPLING_COMPONENT_SERIALIZER}: section codes, RGB reduced to named colours. */
    public static final LegacyComponentSerializer LEGACY_DOWNSAMPLING = LegacyComponentSerializer.builder()
        .character(LegacyComponentSerializer.SECTION_CHAR)
        .build();

    private final boolean signed;
    private final List<Component> pages = new ArrayList<>();
    private Component title;
    private Component author;

    public FusionCoreBookBuilder(boolean signed) {
        this.signed = signed;
    }

    @Override
    public BookMeta$BookMetaBuilder title(Component title) {
        if (this.signed) this.title = title;
        return this;
    }

    @Override
    public BookMeta$BookMetaBuilder author(Component author) {
        if (this.signed) this.author = author;
        return this;
    }

    @Override
    public BookMeta$BookMetaBuilder addPage(Component page) {
        this.pages.add(page);
        return this;
    }

    @Override
    public BookMeta$BookMetaBuilder pages(Component... pages) {
        Collections.addAll(this.pages, pages);
        return this;
    }

    @Override
    public BookMeta$BookMetaBuilder pages(Collection<Component> pages) {
        this.pages.addAll(pages);
        return this;
    }

    @Override
    public Book build() {
        BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(this.signed ? Material.WRITTEN_BOOK : Material.WRITABLE_BOOK);
        ((BookMetaBridge) meta).fusioncore$setTitleAuthor(this.title, this.author);
        ((BookMetaBridge) meta).fusioncore$addPages(this.pages);
        return (Book) (Object) meta;
    }
}
