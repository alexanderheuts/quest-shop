package com.holysweet.questshop.server.commands.util;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.data.ShopCatalog;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

public final class CategoriesCommandUtil {
    private CategoriesCommandUtil() {}

    /** Tab-complete canonical ids only (what the server really uses). */
    public static final SuggestionProvider<CommandSourceStack> CATEGORY_SUGGESTIONS = (ctx, b) -> {
        ShopCatalog.INSTANCE.categories().keySet().stream()
                .map(ResourceLocation::toString)
                .forEach(b::suggest);
        return b.buildFuture();
    };

    /** Coerce bare 'path' to 'questshop:path'. */
    public static ResourceLocation coerceToQS(ResourceLocation raw) {
        return raw.getNamespace().isEmpty()
                ? ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, raw.getPath())
                : raw;
    }
}
