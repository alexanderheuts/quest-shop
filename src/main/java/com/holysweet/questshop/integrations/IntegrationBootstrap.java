package com.holysweet.questshop.integrations;

import com.holysweet.questshop.api.coins.CoinsProvider;
import com.holysweet.questshop.service.CoinsService;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.neoforged.fml.ModList;

public final class IntegrationBootstrap {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Fully-qualified class names to avoid hard deps
    private static final String FTB_TEAMS_INTEGRATION_CLASS =
            "com.holysweet.questshop.integrations.ftbteams.FTBTeamsIntegration";
    private static final String FTB_TEAMS_PROVIDER_CLASS =
            "com.holysweet.questshop.integrations.ftbteams.FTBTeamsCoinsProvider";
    private static final String FTB_QUESTS_INTEGRATION_CLASS =
            "com.holysweet.questshop.integrations.ftbquests.FTBQuestsIntegration";

    private IntegrationBootstrap() {}

    public static void bootstrap() {

        /*
         * FTB Teams Integration
         */
        if (ModList.get().isLoaded("ftbteams")) {
            try {

                // Register TeamCoins property + join/leave handlers
                Class.forName(FTB_TEAMS_INTEGRATION_CLASS)
                        .getMethod("bootstrap")
                        .invoke(null);

                // Activate team-backed coins provider (PlayerTeam/PartyTeam unified)
                Object impl = Class.forName(FTB_TEAMS_PROVIDER_CLASS)
                        .getDeclaredConstructor()
                        .newInstance();
                CoinsService.swapBackend((CoinsProvider) impl);

                LOGGER.info("QuestShop: FTB Teams integration enabled.");
            } catch (Throwable t) {
                LOGGER.error("QuestShop: failed to initialize FTB Teams integration.", t);
            }
        } else {
            LOGGER.info("QuestShop: FTB Teams not present; skipping team coins integration.");
        }

        /*
         * FTB Quest Integration
         */
        if (ModList.get().isLoaded("ftbquests")) {
            try {
                Class.forName(FTB_QUESTS_INTEGRATION_CLASS)
                        .getMethod("bootstrap")
                        .invoke(null);
                LOGGER.info("FTB Quests integration initialized.");
            } catch (Throwable t) {
                LOGGER.error("Failed to initialize FTB Quests integration.", t);
            }
        } else {
            LOGGER.debug("FTB Quests not present; skipping integration bootstrap.");
        }

    }
}
