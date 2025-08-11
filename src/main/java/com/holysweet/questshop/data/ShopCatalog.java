package com.holysweet.questshop.data;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/** Thread-safe, read-only view of the current catalog. */
public final class ShopCatalog {
    public static final ShopCatalog INSTANCE = new ShopCatalog();

    private static final class Snapshot {
        final Map<ResourceLocation, ShopCategory> categoriesById;
        final List<ShopEntry> entries;

        Snapshot(Map<ResourceLocation, ShopCategory> categoriesById, List<ShopEntry> entries) {
            this.categoriesById = categoriesById;
            this.entries = entries;
        }
    }

    private final AtomicReference<Snapshot> ref =
            new AtomicReference<>(new Snapshot(Map.of(), List.of()));

    private ShopCatalog() {}

    /** Replace the entire catalog; inputs are defensively copied and sorted. */
    public void replace(Map<ResourceLocation, ShopCategory> categoriesById, List<ShopEntry> entries) {
        Map<ResourceLocation, ShopCategory> cats = new LinkedHashMap<>(categoriesById);
        List<ShopEntry> list = new ArrayList<>(entries);

        list.sort(Comparator
                .comparingInt((ShopEntry e) -> cats.getOrDefault(e.category(),
                        new ShopCategory(ResourceLocation.fromNamespaceAndPath("questshop","general"), "General", true, 0)).order())
                .thenComparing(e -> e.category().toString())
                .thenComparing(e -> e.itemId().toString()));

        ref.set(new Snapshot(Collections.unmodifiableMap(cats), Collections.unmodifiableList(list)));
    }

    public Map<ResourceLocation, ShopCategory> categories() {
        return ref.get().categoriesById;
    }

    public List<ShopCategory> sortedCategories() {
        var s = ref.get();
        ArrayList<ShopCategory> out = new ArrayList<>(s.categoriesById.values());
        out.sort(Comparator.comparingInt(ShopCategory::order).thenComparing(c -> c.id().toString()));
        return out;
    }

    public List<ShopEntry> allEntries() {
        return ref.get().entries;
    }

    public List<ShopEntry> entriesInCategory(ResourceLocation categoryId) {
        var s = ref.get();
        ArrayList<ShopEntry> out = new ArrayList<>();
        for (ShopEntry e : s.entries) if (e.category().equals(categoryId)) out.add(e);
        return out;
    }
}
