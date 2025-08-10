package com.holysweet.questshop;

import dev.ftb.mods.ftbteams.api.event.PlayerJoinedPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLeftPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.property.IntProperty;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import dev.ftb.mods.ftbteams.data.PlayerTeam;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;


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

    private static void PlayerLeftPartyTeamEvent(@Nullable Team prev, @Nullable Team current, boolean deleted) {
        // If the player leaves the team AND the team is deleted (last player), move the coins with.
        if (prev instanceof PartyTeam && current instanceof PlayerTeam && deleted) {
            add(current, prev.getProperty(COINS));
            set(prev, 0);
            current.markDirty();
            prev.markDirty();
        }
    }

    private static void PlayerJoinedPartyTeamEvent(@Nullable Team prev, @Nullable Team current) {
        // If the player joins a team, add their coins
        if (prev instanceof PlayerTeam && current instanceof PartyTeam) {
            add(current, prev.getProperty(COINS));
            set(prev, 0);
            current.markDirty();
            prev.markDirty();
        }
    }

    private TeamCoins() {}

    public static void PlayerLeftPartyTeamEvent(PlayerLeftPartyTeamEvent playerLeftPartyTeamEvent) {
        PlayerLeftPartyTeamEvent(
                playerLeftPartyTeamEvent.getTeam(),
                playerLeftPartyTeamEvent.getPlayerTeam(),
                playerLeftPartyTeamEvent.getTeamDeleted()
        );
    }

    public static void PlayerJoinedPartyTeamEvent(PlayerJoinedPartyTeamEvent playerJoinedPartyTeamEvent) {
        PlayerJoinedPartyTeamEvent(
                playerJoinedPartyTeamEvent.getPreviousTeam(),
                playerJoinedPartyTeamEvent.getTeam()
        );
    }
}
