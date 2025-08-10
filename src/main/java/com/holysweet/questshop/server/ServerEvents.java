package com.holysweet.questshop.server;

import com.holysweet.questshop.server.commands.HqsCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class ServerEvents {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e) {
        HqsCommands.register(e.getDispatcher(), e.getBuildContext());
    }
}