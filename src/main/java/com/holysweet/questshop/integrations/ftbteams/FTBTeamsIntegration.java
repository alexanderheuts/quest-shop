package com.holysweet.questshop.integrations.ftbteams;

import dev.ftb.mods.ftbteams.api.event.TeamEvent;

/**
 * Registers team properties and party join/leave handlers for both coins and categories.
 */
public final class FTBTeamsIntegration {
    private FTBTeamsIntegration() {}

    public static void bootstrap() {
        // Expose our team properties so FTB Teams persists/syncs them
        TeamEvent.COLLECT_PROPERTIES.register(e -> {
            e.add(TeamCoins.COINS);
            e.add(TeamCategories.CATEGORY_SETTINGS);
        });

        // Coins merge/move rules
        TeamEvent.PLAYER_JOINED_PARTY.register(TeamCoins::PlayerJoinedPartyTeamEvent);
        TeamEvent.PLAYER_LEFT_PARTY.register(TeamCoins::PlayerLeftPartyTeamEvent);

        // Categories merge rules
        TeamEvent.PLAYER_JOINED_PARTY.register(TeamCategories::PlayerJoinedPartyTeamEvent);
        TeamEvent.PLAYER_LEFT_PARTY.register(TeamCategories::PlayerLeftPartyTeamEvent);
    }
}
