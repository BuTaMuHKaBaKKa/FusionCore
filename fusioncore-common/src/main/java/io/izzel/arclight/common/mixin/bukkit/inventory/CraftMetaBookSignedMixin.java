package io.izzel.arclight.common.mixin.bukkit.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.izzel.arclight.common.bridge.bukkit.BookMetaBridge;
import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import io.izzel.arclight.common.mod.util.FusionCoreBookBuilder;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.bukkit.craftbukkit.v.inventory.CraftMetaBookSigned;
import org.bukkit.inventory.meta.BookMeta$BookMetaBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

/**
 * Fusion Core: Paper's Adventure book API on a written book (Paper patch 0010 "Adventure",
 * CraftMetaBookSigned): pages are components, title and author legacy text without RGB.
 */
@Mixin(value = CraftMetaBookSigned.class, remap = false)
public abstract class CraftMetaBookSignedMixin implements BookMetaBridge {

    // @formatter:off
    @Shadow protected String title;
    @Shadow protected String author;
    @Shadow protected List<net.minecraft.network.chat.Component> pages;
    @Shadow @Final static int MAX_PAGES;
    @Shadow public abstract int getPageCount();
    @Shadow public abstract boolean setTitle(String title);
    @Shadow public abstract void setAuthor(String author);
    // @formatter:on

    private boolean fusioncore$isValidPage(int page) {
        return page > 0 && page <= this.getPageCount();
    }

    public Component title() {
        return this.title == null ? null : FusionCoreBookBuilder.LEGACY_DOWNSAMPLING.deserialize(this.title);
    }

    public Book title(Component title) {
        this.setTitle(title == null ? null : FusionCoreBookBuilder.LEGACY_DOWNSAMPLING.serialize(title));
        return (Book) (Object) this;
    }

    public Component author() {
        return this.author == null ? null : FusionCoreBookBuilder.LEGACY_DOWNSAMPLING.deserialize(this.author);
    }

    public Book author(Component author) {
        this.setAuthor(author == null ? null : FusionCoreBookBuilder.LEGACY_DOWNSAMPLING.serialize(author));
        return (Book) (Object) this;
    }

    public Component page(final int page) {
        Preconditions.checkArgument(this.fusioncore$isValidPage(page), "Invalid page number (%s/%s)", page, this.getPageCount());
        return FusionCoreAdventure.asAdventure(this.pages.get(page - 1));
    }

    public void page(final int page, Component data) {
        Preconditions.checkArgument(this.fusioncore$isValidPage(page), "Invalid page number (%s/%s)", page, this.getPageCount());
        this.pages.set(page - 1, FusionCoreAdventure.asVanillaNullToEmpty(data));
    }

    public List<Component> pages() {
        if (this.pages == null) return ImmutableList.of();
        return this.pages.stream().map(FusionCoreAdventure::asAdventure).collect(ImmutableList.toImmutableList());
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
            this.pages.add(FusionCoreAdventure.asVanillaNullToEmpty(page));
        }
    }

    public BookMeta$BookMetaBuilder toBuilder() {
        return new FusionCoreBookBuilder(true);
    }

    @Override
    public void fusioncore$addPages(List<Component> pages) {
        this.addPages(pages.toArray(Component[]::new));
    }

    @Override
    public void fusioncore$setTitleAuthor(Component title, Component author) {
        this.title(title);
        this.author(author);
    }
}
