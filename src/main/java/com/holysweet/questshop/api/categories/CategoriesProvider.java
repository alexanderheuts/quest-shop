package com.holysweet.questshop.api.categories;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/**
 * Pluggable storage for per-player (or per-team) category unlock state.
 * Implementations should ONLY store explicit player state (no defaults).
 */
public interface CategoriesProvider {
    /** Stable identifier for logging/debug. */
    String id();

    /** @return whether the player has explicitly unlocked this category. */
    boolean isUnlocked(ServerPlayer player, ResourceLocation categoryId);

    /** @return all explicitly unlocked categories for this player. */
    Set<ResourceLocation> allUnlocked(ServerPlayer player);

    /**
     * Sets explicit unlock state. Implementations should not mix defaults here.
     * @return true if state actually changed
     */
    boolean setUnlocked(ServerPlayer player, ResourceLocation categoryId, boolean unlocked);

    default boolean unlock(ServerPlayer player, ResourceLocation categoryId) {
        return setUnlocked(player, categoryId, true);
    }

    default boolean lock(ServerPlayer player, ResourceLocation categoryId) {
        return setUnlocked(player, categoryId, false);
    }
}
