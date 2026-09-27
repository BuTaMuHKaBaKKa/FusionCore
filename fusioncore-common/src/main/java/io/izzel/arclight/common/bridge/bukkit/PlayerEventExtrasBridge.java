package io.izzel.arclight.common.bridge.bukkit;

/** Fusion Core: what Paper's constructors of player events carry and what the core reads back from them. */
public interface PlayerEventExtrasBridge {

    /** {@code PlayerItemDamageEvent} (Paper patch 0691). */
    interface ItemDamage {
        void bridge$setOriginalDamage(int originalDamage);
    }

    /** {@code PlayerItemMendEvent} (Paper patch 0815). */
    interface ItemMend {
        void bridge$setConsumedExperience(int consumedExperience);
    }

    /** {@code PlayerExpChangeEvent} (Paper patch 0112). */
    interface ExpChange {
        void bridge$setSource(org.bukkit.entity.Entity source);
    }

    /** {@code PlayerItemConsumeEvent} (Paper patch 0073). */
    interface ItemConsume {
        org.bukkit.inventory.ItemStack bridge$getReplacement();
    }

    /** {@code PlayerPickupItemEvent} (Paper patch 0133). */
    interface Pickup {
        boolean bridge$getFlyAtPlayer();
    }
}
