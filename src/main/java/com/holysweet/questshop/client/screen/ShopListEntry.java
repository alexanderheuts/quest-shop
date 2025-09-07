package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.ClientFX;
import com.holysweet.questshop.client.ClientCategories; // needs to expose isUnlocked(ResourceLocation)
import com.holysweet.questshop.network.payload.BuyEntryPayload;
import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;

import java.util.Optional;

public class ShopListEntry extends ObjectSelectionList.Entry<ShopListEntry> {
    public static final Logger LOGGER = LogUtils.getLogger();
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
        boolean categoryUnlocked = ClientCategories.isUnlocked(this.data.category());
        boolean affordable = ClientCoins.get() >= this.data.cost();


        final int contentTop    = top;
        final int contentBottom = top + rowHeight;

        // Background in the content band only
        gg.fill(left, contentTop, left + rowWidth, contentBottom, hovered ? 0x33FFFFFF : 0x22000000);

        // Icon sits exactly in the 16px content band
        int iconX = left + 2;
        int iconY = top + 1;
        gg.renderItem(this.icon, iconX, iconY);
        gg.renderItemDecorations(mc.font, this.icon, iconX, iconY);

        // Text baseline looks centered in the 16px band
        int textY = contentTop + 6;

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
            gg.fill(left, contentTop, left + rowWidth, contentBottom, 0x66000000);
        } else if (!affordable) {
            gg.fill(left, contentTop, left + rowWidth, contentBottom, 0x1AFF0000);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var over = this.isMouseOver(mouseX, mouseY);

        LOGGER.debug("[ShopListEntry] mouseClicked({}, {}, {}) => over={}, focused={}", mouseX, mouseY, button, over, this.isFocused());
//        if (button != 0) return false;
//
//        // Block click if category isn't unlocked
//        if (!ClientCategories.isUnlocked(this.data.category())) {
//            ClientFX.purchaseError(Component.translatable("questshop.category.locked"));
//            return true;
//        }
//
//        // Then affordability
//        if (ClientCoins.get() < this.data.cost()) {
//            ClientFX.purchaseError(Component.translatable("questshop.buy.no_coins"));
//            return true; // swallow click
//        }
//
//        PacketDistributor.sendToServer(new BuyEntryPayload(
//                this.data.itemId(), this.data.amount(), this.data.cost(), this.data.category()
//        ));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public @NotNull Component getNarration() {
        return Component.literal(this.name.getString());
    }
}
