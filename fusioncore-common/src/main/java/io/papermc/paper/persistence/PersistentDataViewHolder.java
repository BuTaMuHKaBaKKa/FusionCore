// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.persistence;

import org.jetbrains.annotations.ApiStatus;

/**
 * The {@link PersistentDataViewHolder} interface defines an object that can view
 * custom persistent data on it.
 */
@ApiStatus.NonExtendable
public interface PersistentDataViewHolder {

    /**
     * Returns a custom tag container view capable of viewing tags on the object.
     * <p>
     * Note that the tags stored on this container are all stored under their
     * own custom namespace therefore modifying default tags using this
     * {@link PersistentDataViewHolder} is impossible.
     *
     * @return the persistent data container view
     */
    PersistentDataContainerView getPersistentDataContainer();
}
