package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.ClientFX;
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

    public ShopListEntry(ShopEntry entry) {
        this.data = entry;

        Optional<Item> opt = BuiltInRegistries.ITEM.getOptional(entry.itemId());
        this.icon = new ItemStack(opt.orElse(Items.BARRIER), 1);
        this.name = opt.map(i -> Component.translatable(i.getDescriptionId()))
                .orElse(Component.literal(entry.itemId().toString()));
    }

    @Override
    public void render(GuiGraphics gg, int index, int top, int left, int rowWidth, int rowHeight,
                       int mouseX, int mouseY, boolean hovered, float partialTick) {
        boolean affordable = ClientCoins.get() >= this.data.cost();

        // Background
        int bg = hovered ? 0x33FFFFFF : 0x22000000;
        gg.fill(left, top, left + rowWidth, top + rowHeight, bg);

        // Icon (centered vertically)
        int iconX = left + 2;
        int iconY = top + (rowHeight - 16) / 2;
        gg.renderItem(this.icon, iconX, iconY);
        gg.renderItemDecorations(mc.font, this.icon, iconX, iconY);

        // Name (left)
        gg.drawString(mc.font, this.name, left + 24, top + 6, 0xFFFFFF, false);

        // Amount | Cost (right), colored by affordability
        String rightText = "x" + Math.max(1, this.data.amount()) + "  |  " + this.data.cost();
        int priceColor = affordable ? 0xFFD966 : 0xFF5555; // gold vs red
        int rx = left + rowWidth - 6 - mc.font.width(rightText);
        gg.drawString(mc.font, rightText, rx, top + 6, priceColor, false);

        // Subtle red overlay when not affordable
        if (!affordable) {
            gg.fill(left, top, left + rowWidth, top + rowHeight, 0x1AFF0000);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        if (ClientCoins.get() < this.data.cost()) {
            ClientFX.purchaseError(Component.translatable("questshop.buy.no_coins"));
            return true; // swallow click
        }

        PacketDistributor.sendToServer(new BuyEntryPayload(
                this.data.itemId(), this.data.amount(), this.data.cost(), this.data.category()
        ));
        return true;
    }

    @Override
    public @NotNull Component getNarration() {
        return Component.literal(this.name.getString());
    }
}
