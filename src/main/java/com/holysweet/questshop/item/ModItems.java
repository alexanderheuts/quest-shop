package com.holysweet.questshop.item;

import com.holysweet.questshop.QuestShop;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(QuestShop.MODID);

    public static final DeferredItem<Item> COIN = ITEMS.register("coin",
            () -> new CoinItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
