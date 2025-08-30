// src/main/java/com/holysweet/questshop/service/CategoriesService.java
package com.holysweet.questshop.service;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.categories.CategoriesProvider;
import com.holysweet.questshop.api.categories.CategorySetting;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.network.Net;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public final class CategoriesService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicReference<CategoriesProvider> ACTIVE =
            new AtomicReference<>(new VanillaCategoriesProvider());

    private CategoriesService() {}

    public static @NotNull CategoriesProvider provider() { return ACTIVE.get(); }

    public static void swapProvider(@NotNull CategoriesProvider provider) {
        Objects.requireNonNull(provider, "provider");
        var prev = ACTIVE.getAndSet(provider);
        LOGGER.info("QuestShop: categories provider swapped: '{}' -> '{}'",
                prev == null ? "<none>" : prev.id(), provider.id());
    }

    // -------- Definitions --------
    public static Map<ResourceLocation, ShopCategory> categories() { return ShopCatalog.INSTANCE.categories(); }

    public static List<ResourceLocation> categoryOrder() {
        var map = categories();
        return map.keySet().stream()
                .sorted(Comparator
                        .comparingInt((ResourceLocation id) -> map.get(id).order())
                        .thenComparing(ResourceLocation::toString))
                .toList();
    }

    // -------- Effective state --------
    public static boolean isUnlocked(ServerPlayer player, ResourceLocation categoryId) {
        ShopCategory cat = categories().get(categoryId);
        if (cat == null) return false;
        CategorySetting s = provider().getSetting(player, categoryId);
        return switch (s) {
            case UNLOCKED -> true;
            case LOCKED -> false;
            case DEFAULT -> cat.unlockedByDefault();
        };
    }

    public static Set<ResourceLocation> effectiveUnlocked(ServerPlayer player) {
        Set<ResourceLocation> out = new HashSet<>();
        categories().forEach((id, cat) -> {
            if (isUnlocked(player, id)) out.add(id);
        });
        return out;
    }

    // -------- Mutations (auto-sync) --------
    /** Sets UNLOCKED(true) or LOCKED(false). */
    public static boolean setUnlocked(ServerPlayer player, ResourceLocation categoryId, boolean unlocked) {
        CategorySetting s = unlocked ? CategorySetting.UNLOCKED : CategorySetting.LOCKED;
        boolean changed = provider().setSetting(player, categoryId, s);
        if (changed) Net.sendCategoriesSnapshot(player);
        return changed;
    }

    /** Clears to DEFAULT (uses datapack default again). */
    public static boolean clear(ServerPlayer player, ResourceLocation categoryId) {
        boolean changed = provider().setSetting(player, categoryId, CategorySetting.DEFAULT);
        if (changed) Net.sendCategoriesSnapshot(player);
        return changed;
    }
}
