package com.holysweet.questshop.service;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.categories.CategoriesProvider;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.network.Net;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Facade for category definitions (datapacks) and unlock state (provider).
 * Effective unlock = (unlocked_by_default || explicitly unlocked in provider).
 */
public final class CategoriesService {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final AtomicReference<CategoriesProvider> ACTIVE =
            new AtomicReference<>(new VanillaCategoriesProvider());

    private CategoriesService() {}

    public static @NotNull CategoriesProvider provider() {
        return ACTIVE.get();
    }

    public static void swapProvider(@NotNull CategoriesProvider provider) {
        Objects.requireNonNull(provider, "provider");
        CategoriesProvider prev = ACTIVE.getAndSet(provider);
        LOGGER.info("QuestShop: categories provider swapped: '{}' -> '{}'",
                prev == null ? "<none>" : prev.id(), provider.id());
    }

    // ---- Definitions (server authoritative) ----

    public static Map<ResourceLocation, ShopCategory> categories() {
        return ShopCatalog.INSTANCE.categories();
    }

    public static List<ResourceLocation> categoryOrder() {
        var map = categories();
        return map.keySet().stream()
                .sorted(Comparator
                        .comparingInt((ResourceLocation id) -> map.get(id).order())
                        .thenComparing(ResourceLocation::toString))
                .toList();
    }

    // ---- Unlock queries ----

    public static boolean isUnlocked(ServerPlayer player, ResourceLocation categoryId) {
        ShopCategory cat = categories().get(categoryId);
        if (cat == null) return false;
        return cat.unlockedByDefault() || provider().isUnlocked(player, categoryId);
    }

    /** Effective unlocks = defaults ∪ explicit. */
    public static Set<ResourceLocation> effectiveUnlocked(ServerPlayer player) {
        Set<ResourceLocation> out = new HashSet<>();
        categories().forEach((id, cat) -> {
            if (cat.unlockedByDefault() || provider().isUnlocked(player, id)) out.add(id);
        });
        return out;
    }

    // ---- Mutations (+ auto client sync of full snapshot) ----

    public static boolean setUnlocked(ServerPlayer player, ResourceLocation categoryId, boolean unlocked) {
        boolean changed = provider().setUnlocked(player, categoryId, unlocked);
        if (changed) Net.sendCategoriesSnapshot(player);
        return changed;
    }

}
