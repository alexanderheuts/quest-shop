package com.holysweet.questshop.integrations;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.neoforged.fml.ModList;

public final class IntegrationBootstrap {
    private static final Logger LOGGER = LogUtils.getLogger();

    private IntegrationBootstrap() {}

    public static void bootstrap() {
        if (!ModList.get().isLoaded("ftbquests")) {
            LOGGER.debug("FTB Quests not present; skipping integration bootstrap.");
            return;
        }
        try {
            Class.forName("com.holysweet.questshop.integrations.ftbquests.FTBQuestsIntegration")
                    .getMethod("bootstrap")
                    .invoke(null);
            LOGGER.info("FTB Quests integration initialized.");
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize FTB Quests integration.", t);
        }
    }
}
