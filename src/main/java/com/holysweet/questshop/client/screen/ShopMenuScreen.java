package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.menu.ShopMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import com.holysweet.questshop.client.ClientShopData;
import org.jetbrains.annotations.NotNull;

public class ShopMenuScreen extends AbstractContainerScreen<ShopMenu> {
    private ShopList list;

    public ShopMenuScreen(ShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 248; // logical width only
        this.imageHeight = 180;
    }

    @Override
    protected void init() {
        super.init(); // sets leftPos/topPos
        int top = this.topPos + 24;
        int bottom = this.topPos + this.imageHeight - 12;
        int itemHeight = 22;

        int innerX = this.leftPos + 4;
        int innerWidth = this.imageWidth - 8;

        this.list = new ShopList(Minecraft.getInstance(), innerWidth, (bottom - top), top, itemHeight);
        this.list.setX(innerX);
        this.refreshEntries();
        this.addRenderableWidget(this.list);

        // Populate rows from the authoritative catalog (client copy is fine; server validates)
        ShopCatalog.INSTANCE.allEntries().forEach(e -> this.list.add(new ShopListEntry(e)));
    }

    @Override
    public void render(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg, mouseX, mouseY, partialTick);
        super.render(gg, mouseX, mouseY, partialTick); // renders list
        this.renderTooltip(gg, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
        // Title (left)
        gg.drawString(this.font, this.title, 8, 6, 0xFFFFFF, false);

        // Balance (right)
        String coins = "Coins: " + ClientCoins.get();
        int x = this.imageWidth - 8 - this.font.width(coins);
        gg.drawString(this.font, coins, x, 6, 0xFFD966, false);
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        // No textured background; just draw a light panel
        int x0 = this.leftPos;
        int y0 = this.topPos;
        int x1 = x0 + this.imageWidth;
        int y1 = y0 + this.imageHeight;
        gg.fill(x0, y0, x1, y1, 0xAA101010); // translucent dark backdrop
    }

    public void refreshEntries() {
        var rows = ClientShopData.get().stream().map(ShopListEntry::new).toList();
        this.list.setEntries(rows);
    }
}
