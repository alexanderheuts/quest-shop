// src/main/java/com/holysweet/questshop/client/screen/ShopList.java
package com.holysweet.questshop.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;

import java.util.List;

public class ShopList extends ObjectSelectionList<ShopListEntry> {
    private static final int SCROLLBAR_W = 6;
    private final int panelWidth;

    public ShopList(Minecraft mc, int width, int listHeight, int top, int itemHeight) {
        super(mc, width, listHeight, top, itemHeight);
        this.panelWidth = width;
    }

    /** Public helper since addEntry is protected. */
    public void add(ShopListEntry entry) {
        this.addEntry(entry);
    }

    public void setEntries(List<ShopListEntry> entries) {
        this.children().clear();
        for (ShopListEntry e : entries) {
            this.addEntry(e);
        }
        this.setScrollAmount(0);
    }

    /** Make rows start exactly at the list's X (no automatic centering). */
    @Override
    public int getRowLeft() {
        return this.getX();
    }

    /** Let rows span up to the scrollbar (no extra padding). */
    @Override
    public int getRowWidth() {
        return this.panelWidth - SCROLLBAR_W;
    }

    /** Keep the scrollbar flush with the right edge of the list. */
    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.panelWidth - SCROLLBAR_W;
    }
}
