package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.ClientCategories;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ShopListEntry extends ObjectSelectionList.Entry<ShopListEntry> {
    public final ShopEntry data;
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
        boolean categoryUnlocked = ClientCategories.isUnlocked(this.data.category());
        boolean affordable = ClientCoins.get() >= this.data.cost();

        final int contentBottom = top + rowHeight;

        // Background in the content band only
        gg.fill(left, top, left + rowWidth, contentBottom, hovered ? 0x33FFFFFF : 0x22000000);

        // Icon sits exactly in the 16px content band
        int iconX = left + 2;
        int iconY = top + 1;
        gg.renderItem(this.icon, iconX, iconY);
        gg.renderItemDecorations(mc.font, this.icon, iconX, iconY);

        // Text baseline looks centered in the 16px band
        int textY = top + 6;

        // Name (dim if category locked)
        int nameColor = categoryUnlocked ? 0xFFFFFFFF : 0xFFB0B0B0;
        gg.drawString(mc.font, this.name, left + 24, textY, nameColor, false);

        // Amount | Cost (right)
        String rightText = "x" + Math.max(1, this.data.amount()) + "  |  " + this.data.cost();
        int priceColor = !categoryUnlocked ? 0xFFB0B0B0 : (affordable ? 0xFFF1C232 : 0xFFFF5555);
        int rx = left + rowWidth - 6 - mc.font.width(rightText);
        gg.drawString(mc.font, rightText, rx, textY, priceColor, false);

        // Overlays limited to content band
        if (!categoryUnlocked) {
            gg.fill(left, top, left + rowWidth, contentBottom, 0x66000000);
        } else if (!affordable) {
            gg.fill(left, top, left + rowWidth, contentBottom, 0x1AFF0000);
        }
    }

    @Override
    public @NotNull Component getNarration() {
        return Component.literal(this.name.getString());
    }
}
