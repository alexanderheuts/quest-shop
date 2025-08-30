package com.holysweet.questshop.integrations;

import com.holysweet.questshop.api.coins.CoinsProvider;
import com.holysweet.questshop.api.categories.CategoriesProvider;
import com.holysweet.questshop.service.CoinsService;
import com.holysweet.questshop.service.CategoriesService;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

public final class IntegrationBootstrap {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Fully-qualified class names to avoid hard deps
    private static final String FTB_TEAMS_INTEGRATION_CLASS =
            "com.holysweet.questshop.integrations.ftbteams.FTBTeamsIntegration";
    private static final String FTB_TEAMS_COINS_PROVIDER_CLASS =
            "com.holysweet.questshop.integrations.ftbteams.FTBTeamsCoinsProvider";
    private static final String FTB_TEAMS_CATEGORIES_PROVIDER_CLASS =
            "com.holysweet.questshop.integrations.ftbteams.FTBTeamsCategoriesProvider";

    private static final String FTB_QUESTS_INTEGRATION_CLASS =
            "com.holysweet.questshop.integrations.ftbquests.FTBQuestsIntegration";

    private IntegrationBootstrap() {}

    public static void bootstrap() {

        /*
         * FTB Teams Integration
         */
        if (ModList.get().isLoaded("ftbteams")) {
            try {
                // Register team properties + join/leave handlers (coins + categories)
                Class.forName(FTB_TEAMS_INTEGRATION_CLASS)
                        .getMethod("bootstrap")
                        .invoke(null);

                // Activate team-backed coins provider
                Object coinsImpl = Class.forName(FTB_TEAMS_COINS_PROVIDER_CLASS)
                        .getDeclaredConstructor()
                        .newInstance();
                CoinsService.swapBackend((CoinsProvider) coinsImpl);

                // Activate team-backed categories provider
                Object catsImpl = Class.forName(FTB_TEAMS_CATEGORIES_PROVIDER_CLASS)
                        .getDeclaredConstructor()
                        .newInstance();
                CategoriesService.swapProvider((CategoriesProvider) catsImpl);

                LOGGER.info("QuestShop: FTB Teams integration enabled (coins + categories).");
            } catch (Throwable t) {
                LOGGER.error("QuestShop: failed to initialize FTB Teams integration.", t);
            }
        } else {
            LOGGER.info("QuestShop: FTB Teams not present; skipping team integrations.");
        }

        /*
         * FTB Quests Integration
         */
        if (ModList.get().isLoaded("ftbquests")) {
            try {
                Class.forName(FTB_QUESTS_INTEGRATION_CLASS)
                        .getMethod("bootstrap")
                        .invoke(null);
                LOGGER.info("QuestShop: FTB Quests integration initialized.");
            } catch (Throwable t) {
                LOGGER.error("QuestShop: failed to initialize FTB Quests integration.", t);
            }
        } else {
            LOGGER.debug("QuestShop: FTB Quests not present; skipping integration bootstrap.");
        }
    }
}
