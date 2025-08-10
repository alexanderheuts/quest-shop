package com.holysweet.questshop.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import com.holysweet.questshop.registry.ModMenuTypes;
import net.minecraft.world.item.ItemStack;

public class ShopMenu extends AbstractContainerMenu {
    public ShopMenu(int windowId, Inventory playerInv) {
        super(ModMenuTypes.SHOP_MENU.get(), windowId);
        // No slots yet. Later: add coin slot(s) or property sync for balance/stock.
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // You may gate by distance/block/etc. later.
    }
}
