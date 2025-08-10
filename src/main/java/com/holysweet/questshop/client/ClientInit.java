package com.holysweet.questshop.client;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.client.screen.ShopMenuScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.holysweet.questshop.registry.ModMenuTypes;

@EventBusSubscriber(modid = QuestShop.MODID, value = Dist.CLIENT)
public class ClientInit {

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent e) {
        e.register(ModMenuTypes.SHOP_MENU.get(), ShopMenuScreen::new);
    }

}
