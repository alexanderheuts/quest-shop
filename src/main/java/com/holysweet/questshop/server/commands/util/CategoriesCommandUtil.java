package com.holysweet.questshop.server.commands.util;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.data.ShopCatalog;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

public final class CategoriesCommandUtil {
    private CategoriesCommandUtil() {}

    /** Suggest both fully-qualified ids (questshop:building) and bare paths (building) for our namespace. */
    public static final SuggestionProvider<CommandSourceStack> CATEGORY_SUGGESTIONS = (ctx, builder) -> {
        var keys = ShopCatalog.INSTANCE.categories().keySet();
        SharedSuggestionProvider.suggestResource(keys, builder); // full ids for all namespaces
        keys.stream()
                .filter(id -> id.getNamespace().equals(QuestShop.MODID))
                .forEach(id -> builder.suggest(id.getPath()));     // bare paths for our ns
        return builder.buildFuture();
    };

    /** Resolve bare 'building' to 'questshop:building' if present; otherwise return the original id. */
    public static ResourceLocation resolve(ResourceLocation raw) {
        var cats = ShopCatalog.INSTANCE.categories();
        if (cats.containsKey(raw)) return raw;
        var fallback = ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, raw.getPath());
        return cats.containsKey(fallback) ? fallback : raw;
    }
}
