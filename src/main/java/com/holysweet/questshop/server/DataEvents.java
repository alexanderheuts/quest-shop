package com.holysweet.questshop.server;

import com.holysweet.questshop.data.ShopReloader;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber
public final class DataEvents {
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent e) {
        e.addListener(ShopReloader.INSTANCE);
    }
}
