// src/main/java/com/holysweet/questshop/data/ShopData.java
package com.holysweet.questshop.data;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

/**
 * Compatibility facade for legacy references.
 * Delegates to {@link ShopCatalog} as the single source of truth.
 */
public final class ShopData {
    public static final ShopData INSTANCE = new ShopData();

    private ShopData() {}

    /** Replace the entire catalog (delegates to ShopCatalog). */
    public void replace(Map<ResourceLocation, ShopCategory> categoriesById, List<ShopEntry> entries) {
        ShopCatalog.INSTANCE.replace(categoriesById, entries);
    }

    /** Current categories keyed by ID (unmodifiable). */
    public Map<ResourceLocation, ShopCategory> categories() {
        return ShopCatalog.INSTANCE.categories();
    }

    /** Categories sorted by order, then ID. */
    public List<ShopCategory> sortedCategories() {
        return ShopCatalog.INSTANCE.sortedCategories();
    }

    /** All entries (sorted, unmodifiable). */
    public List<ShopEntry> allEntries() {
        return ShopCatalog.INSTANCE.allEntries();
    }

    /** Entries in a specific category (sorted subset). */
    public List<ShopEntry> entriesInCategory(ResourceLocation categoryId) {
        return ShopCatalog.INSTANCE.entriesInCategory(categoryId);
    }
}
