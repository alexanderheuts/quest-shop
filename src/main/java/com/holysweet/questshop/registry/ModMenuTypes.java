package com.holysweet.questshop.registry;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.menu.ShopMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    private ModMenuTypes() {}

    // Register the MENU registry for this mod
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, QuestShop.MODID);

    // Your shop MenuType
    public static final DeferredHolder<MenuType<?>, MenuType<ShopMenu>> SHOP_MENU =
            MENU_TYPES.register("shop",
                    () -> new MenuType<>(ShopMenu::new, FeatureFlags.VANILLA_SET));
}
