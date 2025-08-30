package com.holysweet.questshop.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShopList extends ObjectSelectionList<ShopListEntry> {
    private static final int SCROLLBAR_W     = 6; // visual width of the bar

    private int panelWidth;

    public ShopList(Minecraft mc, int width, int listHeight, int top, int itemHeight) {
        super(mc, width - SCROLLBAR_W, listHeight, top, itemHeight);
        this.panelWidth = width;
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width - SCROLLBAR_W);
        this.panelWidth = width;
    }

    /** Public helper since addEntry is protected. */
    public void add(ShopListEntry entry) {
        this.addEntry(entry);
    }

    public void setEntries(List<ShopListEntry> entries) {
        this.children().clear();
        for (ShopListEntry e : entries) this.addEntry(e);
        this.setScrollAmount(0);
    }

    @Override
    protected void renderListItems(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        int count = this.getItemCount();
        if (count == 0) return;

        int left       = this.getRowLeft();
        int drawWidth  = this.getScrollbarPosition() - left;
        int rowHeight  = this.itemHeight;

        int viewTop    = this.getY();
        int viewBottom = this.getBottom();

        // single top line above first row
        if (count > 0) {
            int firstTop = this.getRowTop(0);
            if (firstTop > viewTop) {
                gfx.fill(left, firstTop - 1, left + drawWidth, firstTop, 0xFF2E2E2E);
            }
        }

        for (int i = 0; i < count; i++) {
            int top = this.getRowTop(i);
            int bottom = top + rowHeight;

            if (bottom >= viewTop && top <= viewBottom) {
                // hovered based on hit region
                boolean hovered = mouseY >= top && mouseY < bottom
                        && mouseX >= left && mouseX < left + drawWidth;

                // draw using full visual width
                ShopListEntry entry = this.children().get(i);
                entry.render(gfx, i, top, left, drawWidth, rowHeight, mouseX, mouseY, hovered, partial);
            }

            // 3px separator: dark–light–dark at full visual width
            int yD1 = bottom - 3, yL = bottom - 2, yD2 = bottom - 1;
            if (yD1 >= viewTop && yD1 < viewBottom) gfx.fill(left, yD1, left + drawWidth, yD1 + 1, 0xFF1E1E1E);
            if (yL  >= viewTop && yL  < viewBottom) gfx.fill(left, yL,  left + drawWidth, yL  + 1, 0xFF3A3A3A);
            if (yD2 >= viewTop && yD2 < viewBottom) gfx.fill(left, yD2, left + drawWidth, yD2 + 1, 0xFF1E1E1E);
        }
    }


    /** Make rows start exactly at the list's X (no automatic centering). */
    @Override
    public int getRowLeft() {
        return this.getX();
    }

    /** Let rows stop well before the scrollbar so hover/clicks never leak under it. */
    @Override
    public int getRowWidth() {
        return super.getRowWidth();
    }

    /** Keep the scrollbar flush with the right edge of the list. */
    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.panelWidth - SCROLLBAR_W;
    }
}
