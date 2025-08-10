package com.holysweet.questshop.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ShopListEntry extends ObjectSelectionList.Entry<ShopListEntry> {
    private final String name;
    private final int amount;
    private final int cost;

    public ShopListEntry(String name, int amount, int cost) {
        this.name = name;
        this.amount = amount;
        this.cost = cost;
    }

    @Override
    public void render(GuiGraphics gg, int index, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float partialTick) {
        var font = Minecraft.getInstance().font;

        int left   = x + 8;
        int center = x + entryWidth / 2;
        int right  = x + entryWidth - 8;

        gg.drawString(font, name, left, y + 6, 0xFFFFFF, false);

        String amt = "x" + amount;
        int amtW = font.width(amt);
        gg.drawString(font, amt, x + entryWidth - 8 - amtW, y + 6, 0xAAAAAA, false);

        String costStr = cost + " ⛁";
        int costW = font.width(costStr);
        gg.drawString(font, costStr, x + entryWidth - 8 - costW - 8 - amtW, y + 6, 0xFFD166, false);
//        gg.drawString(font, "x" + amount, center - 20, y + 6, 0xAAAAAA, false);
//        gg.drawString(font, cost + " ⛁", right - 24, y + 6, 0xFFD166, false);
    }

    @Override
    public @NotNull Component getNarration() {
        return Component.literal(name + " amount " + amount + " cost " + cost);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // TODO: handle click
        return false;
    }
}
