package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCategories;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.ClientFX;
import com.holysweet.questshop.menu.ShopMenu;
import com.holysweet.questshop.network.payload.BuyEntryPayload;
import com.holysweet.questshop.network.payload.BuyResultPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import com.holysweet.questshop.client.ClientShopData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ShopMenuScreen extends AbstractContainerScreen<ShopMenu> {

    public static final Logger LOGGER = LogUtils.getLogger();

    private static final int FOOTER_HEIGHT = 28;
    private static final int HEADER_HEIGHT = 24;
    private static final int MARGIN = 16;
    private static final int WIDTH = 248;

    private ShopList list;

    private Button buyButton;
    private boolean purchasePending = false;

    public ShopMenuScreen(ShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = WIDTH;
    }

    @Override
    protected void init() {
        this.imageHeight = this.height - 2 * MARGIN;

        super.init(); // sets leftPos/topPos

        int top = this.topPos + HEADER_HEIGHT;
        int bottom = this.topPos + this.imageHeight - FOOTER_HEIGHT;
        int itemHeight = 18;

        int innerX = this.leftPos;
        int innerWidth = this.imageWidth;

        this.list = new ShopList(Minecraft.getInstance(), innerWidth, (bottom - top), top, itemHeight);
        this.list.setX(innerX);
        this.refreshEntries();
        this.addRenderableWidget(this.list);

        // Populate rows from the authoritative catalog (client copy is fine; server validates)
        this.refreshEntries();

        int btnW = (int)Math.round(this.imageWidth * 0.65); // ~65% width
        int btnH = 20;
        int btnX = this.leftPos + (this.imageWidth - btnW) / 2;
        int btnY = this.topPos + this.imageHeight - btnH - 4; // bottom padding

        this.buyButton = Button.builder(Component.literal("Buy"), this::buttonClick)
                .bounds(btnX, btnY, btnW, btnH)
                .build();
        this.buyButton.active = false; // disabled until an item is selected

        this.addRenderableWidget(this.buyButton);

        this.list.setOnSelectionChanged(this::updateButtonState);
    }

    public void refreshEntries() {
        var rows = ClientShopData.get().stream().map(ShopListEntry::new).toList();
        this.list.setEntries(rows);
    }

    /* ---------- Rendering ---------- */

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

    /* ---------- Networking Helpers ---------- */

    public void onPurchaseResult(BuyResultPayload.Code code) {
        this.purchasePending = false;
        switch (code) {
            case INVALID_ENTRY -> ClientFX.purchaseError(Component.translatable("questshop.buy.invalid"));
            case NOT_ENOUGH_COINS -> ClientFX.purchaseError(Component.translatable("questshop.buy.no_coins"));
            case NO_INVENTORY_SPACE -> ClientFX.purchaseError(Component.translatable("questshop.buy.no_space"));
            case LOCKED_CATEGORY -> ClientFX.purchaseError(Component.translatable("questshop.buy.locked"));
            case OK -> { /* handled by onPurchaseOk(...) */ }
        }
        updateButtonState();
    }

    public void onPurchaseOk(ResourceLocation itemId, int amount, int cost) {
        this.purchasePending = false;
        ClientFX.purchaseOk(itemId, amount, cost);
        updateButtonState();
    }

    /* ---------- Button Helpers ---------- */

    public void updateButtonState() {
        if (this.buyButton == null) return;

        boolean active = false;

        if (!this.purchasePending) {
            var selected = (this.list != null) ? this.list.getSelected() : null;
            if (selected != null) {
                ShopEntry entry = selected.data;
                active = ClientCategories.isUnlocked(entry.category())
                        && ClientCoins.get() >= entry.cost();
            }
        }

        this.buyButton.active = active;
    }

    private void buttonClick(Button button) {
        if (purchasePending) return;            // prevent double-clicks

        purchasePending = true;
        button.active = false;

        var selected = (this.list != null) ? this.list.getSelected() : null;
        if (selected == null) {
            purchasePending = false;
            updateButtonState();
            return;
        }

        ShopEntry entry = selected.data;
        PacketDistributor.sendToServer(new BuyEntryPayload(
                entry.itemId(), entry.amount(), entry.cost(), entry.category()
        ));
    }

    /* ---------- Screen Helpers ---------- */

    @Override
    public void removed() {
        super.removed();
        this.purchasePending = false;
    }
}
