package com.holysweet.questshop.integrations.ftbteams;

import dev.ftb.mods.ftbteams.api.event.TeamEvent;

/**
 * Called only when 'ftbteams' is loaded.
 * Keep all direct FTB Teams references inside this class.
 */
public final class FTBTeamsIntegration {
    private FTBTeamsIntegration() {}

    public static void bootstrap() {
        // Property collection
        TeamEvent.COLLECT_PROPERTIES.register(ev -> ev.add(TeamCoins.COINS));

        // Movement between teams keeps coins consistent
        TeamEvent.PLAYER_LEFT_PARTY.register(TeamCoins::PlayerLeftPartyTeamEvent);
        TeamEvent.PLAYER_JOINED_PARTY.register(TeamCoins::PlayerJoinedPartyTeamEvent);
    }
}