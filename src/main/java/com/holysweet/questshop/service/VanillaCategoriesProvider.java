// src/main/java/com/holysweet/questshop/service/VanillaCategoriesProvider.java
package com.holysweet.questshop.service;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.categories.CategoriesProvider;
import com.holysweet.questshop.api.categories.CategorySetting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class VanillaCategoriesProvider implements CategoriesProvider {
    private static final String NBT_SETTINGS = QuestShop.MODID + ":category_settings";
    // Stored as CompoundTag: key = category id, value = "U" or "L" (no entry = DEFAULT)

    @Override public @NotNull String id() { return "vanilla"; }

    private static Map<ResourceLocation, CategorySetting> read(CompoundTag root) {
        if (!root.contains(NBT_SETTINGS, Tag.TAG_COMPOUND)) return Map.of();
        CompoundTag m = root.getCompound(NBT_SETTINGS);
        Map<ResourceLocation, CategorySetting> out = new HashMap<>(m.getAllKeys().size());
        for (String k : m.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(k);
            if (id != null) {
                String v = m.getString(k);
                if ("U".equals(v)) out.put(id, CategorySetting.UNLOCKED);
                else if ("L".equals(v)) out.put(id, CategorySetting.LOCKED);
            }
        }
        return out;
    }

    private static void write(CompoundTag root, Map<ResourceLocation, CategorySetting> map) {
        CompoundTag m = new CompoundTag();
        map.forEach((id, val) -> m.putString(id.toString(), (val == CategorySetting.UNLOCKED) ? "U" : "L"));
        root.put(NBT_SETTINGS, m);
    }

    @Override
    public CategorySetting getSetting(ServerPlayer player, ResourceLocation categoryId) {
        CategorySetting s = read(player.getPersistentData()).get(categoryId);
        return (s == null) ? CategorySetting.DEFAULT : s;
    }

    @Override
    public boolean setSetting(ServerPlayer player, ResourceLocation categoryId, CategorySetting setting) {
        CompoundTag root = player.getPersistentData();
        Map<ResourceLocation, CategorySetting> map = new HashMap<>(read(root));
        boolean changed;
        if (setting == CategorySetting.DEFAULT) {
            changed = (map.remove(categoryId) != null);
        } else {
            CategorySetting prev = map.put(categoryId, setting);
            changed = !Objects.equals(prev, setting);
        }
        if (changed) write(root, map);
        return changed;
    }
}
