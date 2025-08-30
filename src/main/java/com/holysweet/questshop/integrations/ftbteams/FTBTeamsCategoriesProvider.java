package com.holysweet.questshop.integrations.ftbteams;

import com.holysweet.questshop.api.categories.CategoriesProvider;
import com.holysweet.questshop.api.categories.CategorySetting;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public final class FTBTeamsCategoriesProvider implements CategoriesProvider {
    @Override public @NotNull String id() { return "ftbteams"; }

    private static Team teamFor(ServerPlayer player) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElse(null);
    }

    @Override
    public CategorySetting getSetting(ServerPlayer player, ResourceLocation categoryId) {
        Team t = teamFor(player);
        return (t == null) ? CategorySetting.DEFAULT : TeamCategories.getSetting(t, categoryId);
    }

    @Override
    public boolean setSetting(ServerPlayer player, ResourceLocation categoryId, CategorySetting setting) {
        Team t = teamFor(player);
        if (t == null) return false;
        return TeamCategories.setSetting(t, categoryId, setting);
    }
}
