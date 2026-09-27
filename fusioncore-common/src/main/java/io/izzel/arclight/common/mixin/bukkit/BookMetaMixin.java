package io.izzel.arclight.common.mixin.bukkit;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta$BookMetaBuilder;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/**
 * Fusion Core: {@link BookMeta} is an Adventure {@link Book}, as in Paper. The builder-style methods are declared with
 * {@code Book} return types; {@code PaperCallTransformer} redirects plugin calls.
 */
@Mixin(value = BookMeta.class, remap = false)
public interface BookMetaMixin extends Book {

    @Override
    default Component title() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    @Override
    default Book title(Component title) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    @Override
    default Component author() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    @Override
    default Book author(Component author) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    @Override
    default List<Component> pages() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    @Override
    default Book pages(List<Component> pages) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    @Override
    default Book pages(Component... pages) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default Component page(int page) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default void page(int page, Component data) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default void addPages(Component... pages) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    @Override
    default BookMeta$BookMetaBuilder toBuilder() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }
}
