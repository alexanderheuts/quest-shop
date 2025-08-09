package com.holysweet.questshop;

import dev.ftb.mods.ftbteams.api.property.IntProperty;
import dev.ftb.mods.ftbteams.api.Team;

import net.minecraft.resources.ResourceLocation;

public final class TeamCoins {
    public static final IntProperty COINS = new IntProperty(
            ResourceLocation.fromNamespaceAndPath("questshop", "coins"), 0
    );

    public static int get(Team team) {
        return team.getProperty(COINS);
    }

    public static void set(Team team, int value) {
        team.setProperty(COINS, Math.max(0, value));
    }

    public static void add(Team team, int delta) {
        set(team, get(team) + delta);
    }

    private TeamCoins() {}
}
