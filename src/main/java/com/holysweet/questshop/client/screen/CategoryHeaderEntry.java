package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.client.ClientCategories;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CategoryHeaderEntry extends BaseShopListEntry {
    private final Minecraft mc = Minecraft.getInstance();
    private final ResourceLocation categoryId;
    private final Component title;

    public CategoryHeaderEntry(ResourceLocation categoryId, String displayName) {
        this.categoryId = categoryId;
        // Use the JSON "display" name directly, or fall back to translated key / path
        if (displayName != null && !displayName.isBlank()) {
            this.title = Component.literal(displayName);
        } else {
            this.title = Component.translatableWithFallback(
                    categoryId.getNamespace() + ".category." + categoryId.getPath(),
                    categoryId.getPath().replace('_', ' ').toUpperCase()
            );
        }
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public void render(GuiGraphics gg, int index, int top, int left, int rowWidth, int rowHeight,
                       int mouseX, int mouseY, boolean hovered, float partialTick) {
        boolean unlocked = ClientCategories.isUnlocked(this.categoryId);

        int textY = top + (rowHeight - mc.font.lineHeight) / 2;
        int textX = left + 8;

        // Show a lock indicator or dimmed color if locked
        Component displayTitle = unlocked ? this.title : Component.literal("🔒 ").append(this.title);
        int titleWidth = mc.font.width(displayTitle);

        int lineStart = textX + titleWidth + 6;
        int lineEnd = left + rowWidth - 6;
        int centerY = top + (rowHeight / 2);

        // Gold for unlocked, muted gray for locked
        int titleColor = unlocked ? 0xFFE0B034 : 0xFF888888;
        int lineColor  = unlocked ? 0x44FFFFFF : 0x22888888;

        gg.drawString(mc.font, displayTitle, textX, textY, titleColor, false);

        if (lineEnd > lineStart) {
            gg.fill(lineStart, centerY, lineEnd, centerY + 1, lineColor);
        }
    }

    @Override
    public @NotNull Component getNarration() {
        return this.title;
    }
}