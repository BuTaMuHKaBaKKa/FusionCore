// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.entity;

import org.bukkit.entity.Fish;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a fish that can school with other fish.
 */
public interface SchoolableFish extends Fish {

    /**
     * Forces this fish to follow the given fish.
     *
     * @param leader fish to follow
     */
    void startFollowing(SchoolableFish leader);

    /**
     * Causes the fish to stop following their current
     * leader.
     */
    void stopFollowing();

    /**
     * Gets the amount of fish currently following this fish.
     *
     * @return school size
     */
    int getSchoolSize();

    /**
     * Gets the maximum number of fish that will naturally follow this fish.
     *
     * @return max school size
     */
    int getMaxSchoolSize();

    /**
     * Gets the fish that this entity is currently following.
     *
     * @return following fish
     */
    @Nullable SchoolableFish getSchoolLeader();

}
