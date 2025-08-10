package com.holysweet.questshop.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;

public class ShopList extends ObjectSelectionList<ShopListEntry> {
    private final int left;

    public ShopList(Minecraft mc,
                    int width,
                    int height,
                    int left,
                    int top,
                    int itemHeight) {
        // 1.21 signature
        super(mc, width, height, top, itemHeight);
        this.left = left;
    }

    /** Public wrappers (super methods are protected). */
    public void addRow(ShopListEntry entry) { super.addEntry(entry); }
    public void clearRows()                { super.clearEntries(); }

    @Override public int  getRowWidth()            { return this.width - 10; }
    @Override public int  getRowLeft()             { return this.left + 5; }
    @Override protected int getScrollbarPosition() { return this.left + this.width - 6; }

    // Silence vanilla decorations/background so the list stays inside your panel.
    @Override protected void renderDecorations(GuiGraphics g, int mouseX, int mouseY) {}
    @Override protected void renderListBackground(GuiGraphics g) {}
}
