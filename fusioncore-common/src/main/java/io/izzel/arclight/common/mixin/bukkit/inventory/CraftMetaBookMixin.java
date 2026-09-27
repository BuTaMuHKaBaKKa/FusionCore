package io.izzel.arclight.common.mixin.bukkit.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.izzel.arclight.common.bridge.bukkit.BookMetaBridge;
import io.izzel.arclight.common.mod.util.FusionCoreBookBuilder;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.craftbukkit.v.inventory.CraftMetaBook;
import org.bukkit.inventory.meta.BookMeta$BookMetaBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

/**
 * Fusion Core: Paper's Adventure book API on a writable book (Paper patch 0010 "Adventure", CraftMetaBook).
 * A writable book has no title or author; its pages are plain text, stored as legacy strings.
 */
@Mixin(value = CraftMetaBook.class, remap = false)
public abstract class CraftMetaBookMixin implements BookMetaBridge {

    // @formatter:off
    @Shadow protected List<String> pages;
    @Shadow @Final static int MAX_PAGES;
    @Shadow public abstract int getPageCount();
    // @formatter:on

    private boolean fusioncore$isValidPage(int page) {
        return page > 0 && page <= this.getPageCount();
    }

    public Component title() {
        return null;
    }

    public Book title(Component title) {
        return (Book) (Object) this;
    }

    public Component author() {
        return null;
    }

    public Book author(Component author) {
        return (Book) (Object) this;
    }

    public Component page(final int page) {
        Preconditions.checkArgument(this.fusioncore$isValidPage(page), "Invalid page number (%s/%s)", page, this.getPageCount());
        return LegacyComponentSerializer.legacySection().deserialize(this.pages.get(page - 1));
    }

    public void page(final int page, Component data) {
        Preconditions.checkArgument(this.fusioncore$isValidPage(page), "Invalid page number (%s/%s)", page, this.getPageCount());
        if (data == null) {
            data = Component.empty();
        }
        this.pages.set(page - 1, LegacyComponentSerializer.legacySection().serialize(data));
    }

    public List<Component> pages() {
        if (this.pages == null) return ImmutableList.of();
        return this.pages.stream().map(LegacyComponentSerializer.legacySection()::deserialize).collect(ImmutableList.toImmutableList());
    }

    public Book pages(List<Component> pages) {
        if (this.pages != null) this.pages.clear();
        for (Component page : pages) {
            this.addPages(page);
        }
        return (Book) (Object) this;
    }

    public Book pages(Component... pages) {
        if (this.pages != null) this.pages.clear();
        this.addPages(pages);
        return (Book) (Object) this;
    }

    public void addPages(Component... pages) {
        if (this.pages == null) this.pages = new ArrayList<>();
        for (Component page : pages) {
            if (this.pages.size() >= MAX_PAGES) {
                return;
            }
            if (page == null) {
                page = Component.empty();
            }
            this.pages.add(LegacyComponentSerializer.legacySection().serialize(page));
        }
    }

    public BookMeta$BookMetaBuilder toBuilder() {
        return new FusionCoreBookBuilder(false);
    }

    @Override
    public void fusioncore$addPages(List<Component> pages) {
        this.addPages(pages.toArray(Component[]::new));
    }
}
