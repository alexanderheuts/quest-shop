package com.holysweet.questshop.client.screen;

import net.minecraft.client.gui.components.ObjectSelectionList;

public abstract class BaseShopListEntry extends ObjectSelectionList.Entry<BaseShopListEntry> {
    public boolean isSelectable() {
        return true;
    }
}