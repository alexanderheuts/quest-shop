package com.holysweet.questshop.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;

import java.util.List;

public class ShopList extends ObjectSelectionList<ShopListEntry> {
    private final int panelWidth;

    public ShopList(Minecraft mc, int width, int listHeight, int top, int itemHeight) {
        super(mc, width, listHeight, top, itemHeight);
        this.panelWidth = width;
    }

    /** Public helper since addEntry is protected. */
    public void add(ShopListEntry entry) {
        this.addEntry(entry);
    }

    /** Fallback for replaceEntries(): clear and re-add. */
    public void setEntries(List<ShopListEntry> entries) {
        this.children().clear();        // clear existing rows
        for (ShopListEntry e : entries) {
            this.addEntry(e);           // re-add each row
        }
        this.setScrollAmount(0);        // reset scroll so content is visible
    }

    @Override
    public int getRowWidth() {
        return this.panelWidth - 16;    // keep padding from scrollbar
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.panelWidth - 6;
    }
}
