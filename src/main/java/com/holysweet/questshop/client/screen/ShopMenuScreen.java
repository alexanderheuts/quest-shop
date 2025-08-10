package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.menu.ShopMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ShopMenuScreen extends AbstractContainerScreen<ShopMenu> {
    private ShopList list;

    public ShopMenuScreen(ShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 248;
        this.imageHeight = 180;
    }

    @Override
    protected void init() {
        super.init(); // sets leftPos/topPos

        int listLeft   = this.leftPos;
        int listTop    = this.topPos + 24;
        int listBottom = this.topPos + this.imageHeight - 12;
        int itemHeight = 22;
        int listHeight = listBottom - listTop;

        this.list = new ShopList(
                this.minecraft,
                this.imageWidth,
                listHeight,   // height of the list area
                listLeft,
                listTop,
                itemHeight
        );
        this.addRenderableWidget(this.list);

        // Placeholder rows — swap with real data later
        this.list.addRow(new ShopListEntry("Iron Sword", 1, 12));
        this.list.addRow(new ShopListEntry("Healing Potion", 3, 9));
        this.list.addRow(new ShopListEntry("Ender Pearl", 4, 20));
        this.list.addRow(new ShopListEntry("Torch x16", 16, 2));
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        int x0 = this.leftPos;
        int y0 = this.topPos;
        int x1 = x0 + this.imageWidth;
        int y1 = y0 + this.imageHeight;
        gg.fill(x0, y0, x1, y1, 0x88000000);
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
        gg.drawString(this.font, this.title, 8, 6, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
//        this.renderBackground(gg, mouseX, mouseY, partialTick);
        super.render(gg, mouseX, mouseY, partialTick); // renders widgets (list)
        this.renderTooltip(gg, mouseX, mouseY);
    }
}
