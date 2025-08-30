package com.holysweet.questshop.api.categories;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface CategoriesProvider {
    String id();

    /** Return the stored setting (DEFAULT if no explicit setting exists). */
    CategorySetting getSetting(ServerPlayer player, ResourceLocation categoryId);

    /**
     * Persist a setting. Use DEFAULT to clear and fall back to datapack default.
     * @return true if the stored value changed
     */
    boolean setSetting(ServerPlayer player, ResourceLocation categoryId, CategorySetting setting);
}
