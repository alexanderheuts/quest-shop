package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.menu.ShopMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ShopMenuScreen extends AbstractContainerScreen<ShopMenu> {
    private ShopList list;

    public ShopMenuScreen(ShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 248;
        this.imageHeight = 180;
    }

    @Override
    protected void init() {
        super.init();

        int listLeft   = this.leftPos;
        int listTop    = this.topPos + 24;
        int listBottom = this.topPos + this.imageHeight - 12;
        int itemHeight = 22;
        int listHeight = Math.max(0, listBottom - listTop);

        this.list = new ShopList(this.minecraft, this.imageWidth, listHeight, listLeft, listTop, itemHeight);
        this.addRenderableWidget(this.list);

        Map<ResourceLocation, ShopCategory> cats = ShopCatalog.INSTANCE.categories();
        List<ShopCategory> ordered = ShopCatalog.INSTANCE.sortedCategories();

        for (ShopCategory cat : ordered) {
            if (!cat.unlockedByDefault()) continue;
            for (ShopEntry def : ShopCatalog.INSTANCE.entriesInCategory(cat.id())) {
                String displayName = def.itemId().toString();
                Optional<Item> item = BuiltInRegistries.ITEM.getOptional(def.itemId());
                if (item.isPresent()) {
                    ItemStack stack = new ItemStack(item.get(), Math.max(1, def.amount()));
                    displayName = stack.getHoverName().getString();
                }
                this.list.addRow(new ShopListEntry(displayName, def.amount(), def.cost()));
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        int x0 = this.leftPos, y0 = this.topPos;
        int x1 = x0 + this.imageWidth, y1 = y0 + this.imageHeight;
        gg.fill(x0, y0, x1, y1, 0x88000000);
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
        gg.drawString(this.font, this.title, 8, 6, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg, mouseX, mouseY, partialTick);
        super.render(gg, mouseX, mouseY, partialTick);
        this.renderTooltip(gg, mouseX, mouseY);
    }
}
