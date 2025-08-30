package com.holysweet.questshop.client;

import com.holysweet.questshop.api.ShopCategory;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Client-side cache for categories + effective unlocks (per current player).
 * Populated by CategoriesSnapshotPayload on S2C.
 */
public final class ClientCategories {
    private static Map<ResourceLocation, ShopCategory> CATEGORIES = Collections.emptyMap();
    private static Set<ResourceLocation> UNLOCKED = Collections.emptySet();

    private ClientCategories() {}

    /** Replace the current snapshot (defensive copies; thread-confined via Net.enqueueWork). */
    public static void applySnapshot(Map<ResourceLocation, ShopCategory> cats,
                                     Set<ResourceLocation> unlocked) {
        CATEGORIES = Collections.unmodifiableMap(new LinkedHashMap<>(cats));
        UNLOCKED   = Collections.unmodifiableSet(new LinkedHashSet<>(unlocked));
    }

    /** True if the category is effectively unlocked for the current player. */
    public static boolean isUnlocked(ResourceLocation categoryId) {
        return UNLOCKED.contains(categoryId);
    }

    /** Unmodifiable view of all categories (as sent by server). */
    public static Map<ResourceLocation, ShopCategory> categories() {
        return CATEGORIES;
    }
}
