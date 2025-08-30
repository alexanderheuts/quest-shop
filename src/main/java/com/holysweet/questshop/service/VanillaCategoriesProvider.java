package com.holysweet.questshop.service;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.categories.CategoriesProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/** Default per-player storage (explicit unlocks only) in persistent NBT. */
public final class VanillaCategoriesProvider implements CategoriesProvider {
    private static final String NBT_KEY = QuestShop.MODID + ":unlocked_categories";

    @Override public @NotNull String id() { return "vanilla"; }

    @Override
    public boolean isUnlocked(ServerPlayer player, ResourceLocation categoryId) {
        return allUnlocked(player).contains(categoryId);
    }

    @Override
    public Set<ResourceLocation> allUnlocked(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(NBT_KEY, Tag.TAG_LIST)) return Set.of();
        ListTag list = tag.getList(NBT_KEY, Tag.TAG_STRING);
        Set<ResourceLocation> out = new HashSet<>(list.size());
        for (Tag t : list) {
            ResourceLocation id = ResourceLocation.tryParse(t.getAsString());
            if (id != null) out.add(id);
        }
        return out;
    }

    @Override
    public boolean setUnlocked(ServerPlayer player, ResourceLocation categoryId, boolean unlocked) {
        Set<ResourceLocation> set = new HashSet<>(allUnlocked(player));
        boolean changed = unlocked ? set.add(categoryId) : set.remove(categoryId);
        if (!changed) return false;

        ListTag list = new ListTag();
        for (ResourceLocation id : set) list.add(StringTag.valueOf(id.toString()));
        player.getPersistentData().put(NBT_KEY, list);
        return true;
    }
}
