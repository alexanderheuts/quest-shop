package com.holysweet.questshop;

import com.holysweet.questshop.quests.CoinsReward;
import com.holysweet.questshop.commands.CoinsCommands;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.common.CommandRegistrationEvent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands.CommandSelection;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import dev.ftb.mods.ftbteams.api.event.*;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(QuestShop.MODID)
public class QuestShop {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "questshop";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public QuestShop(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        TeamEvent.COLLECT_PROPERTIES.register(this::onTeamCollectProperties);
        CoinsReward.bootstrap(); // triggers reward registration

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        CommandRegistrationEvent.EVENT.register(this::registerCommands);


        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void onTeamCollectProperties(TeamCollectPropertiesEvent event) {
        event.add(TeamCoins.COINS);
    }

    private void registerCommands(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext ctx,
            CommandSelection selection
    ) {
        // Your custom coins commands
        CoinsCommands.register(dispatcher, ctx);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
