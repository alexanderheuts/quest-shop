// src/main/java/com/holysweet/questshop/client/screen/ShopListEntry.java
package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.network.payload.BuyEntryPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ShopListEntry extends ObjectSelectionList.Entry<ShopListEntry> {
    private final ShopEntry data;
    private final Minecraft mc = Minecraft.getInstance();
    private final ItemStack icon;
    private final Component name;
    private final String rightText; // "x{amount}  |  {cost}"

    public ShopListEntry(ShopEntry entry) {
        this.data = entry;

        // Resolve item; fall back to barrier if missing
        Optional<Item> opt = BuiltInRegistries.ITEM.getOptional(entry.itemId());
        Item displayItem = opt.orElse(Items.BARRIER);

        this.icon = new ItemStack(displayItem, 1);

        String display = opt
                .map(i -> Component.translatable(i.getDescriptionId()).getString())
                .orElse(entry.itemId().toString());
        this.name = Component.literal(display);

        this.rightText = "x" + Math.max(1, entry.amount()) + "  |  " + entry.cost();
    }

    @Override
    public void render(GuiGraphics gg, int index, int top, int left, int rowWidth, int rowHeight,
                       int mouseX, int mouseY, boolean hovered, float partialTick) {
        int bg = hovered ? 0x33FFFFFF : 0x22000000;
        gg.fill(left, top, left + rowWidth, top + rowHeight, bg);

        // Item Icon
        int iconX = left + 2;
        int iconY = top + (rowHeight - 16) / 2; // item icon is 16px tall
        gg.renderItem(this.icon, iconX, iconY);

        // Item name
        gg.drawString(mc.font, this.name, left + 24, top + 6, 0xFFFFFF, false);

        // Item count & cost
        int rx = left + rowWidth - 6 - mc.font.width(this.rightText);
        gg.drawString(mc.font, this.rightText, rx, top + 6, 0xFFD966, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        PacketDistributor.sendToServer(new BuyEntryPayload(
                this.data.itemId(), this.data.amount(), this.data.cost(), this.data.category()
        ));
        return true;
    }

    @Override
    public @NotNull Component getNarration() {
        return Component.literal(this.name.getString() + " " + this.rightText);
        // For full accessibility later, switch to a translatable with placeholders.
    }
}
