// src/main/java/com/holysweet/questshop/client/screen/ShopList.java
package com.holysweet.questshop.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Override
    protected void renderListItems(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        int count = this.getItemCount();
        if (count == 0) return;

        int left      = this.getRowLeft();
        int rowWidth  = this.getRowWidth();
        int rowHeight = this.itemHeight;

        int viewTop    = this.getY();
        int viewBottom = this.getBottom();

        // (optional) compact the header gap above the first row
        if (count > 0) {
            int firstTop = this.getRowTop(0);
            if (firstTop > viewTop) {
                gfx.fill(left, firstTop - 1, left + rowWidth, firstTop, 0xFF2E2E2E); // single top line
            }
        }

        for (int i = 0; i < count; i++) {
            int top = this.getRowTop(i);
            int bottom = top + rowHeight;

            if (bottom >= viewTop && top <= viewBottom) {
                // (gfx, mouseX, mouseY, partial, index, left, top, width, height)
                this.renderItem(gfx, mouseX, mouseY, partial, i, left, top, rowWidth, rowHeight);
            }

            // 3px separator inside this row’s last 3 pixels: dark–light–dark
            int yD1 = bottom - 3;
            int yL  = bottom - 2;
            int yD2 = bottom - 1;

            if (yD1 >= viewTop && yD1 < viewBottom) gfx.fill(left, yD1, left + rowWidth, yD1 + 1, 0xFF1E1E1E);
            if (yL  >= viewTop && yL  < viewBottom) gfx.fill(left, yL,  left + rowWidth, yL  + 1, 0xFF3A3A3A);
            if (yD2 >= viewTop && yD2 < viewBottom) gfx.fill(left, yD2, left + rowWidth, yD2 + 1, 0xFF1E1E1E);
        }
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
