// src/main/java/com/holysweet/questshop/client/toast/PurchaseToast.java
package com.holysweet.questshop.client.toast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class PurchaseToast implements Toast {
    private static final int WIDTH = 200;
    private static final int HEIGHT = 32;
    private static final long DISPLAY_MS = 3000;

    private final ItemStack icon;
    private final Component title;
    private final Component desc;
    private long startTime = -1L;
    private boolean playedSound = false;

    public PurchaseToast(ItemStack icon, Component title, Component desc) {
        this.icon = icon;
        this.title = title;
        this.desc = desc;
    }

    @Override
    public @NotNull Visibility render(@NotNull GuiGraphics gg, @NotNull ToastComponent component, long time) {
        if (startTime < 0L) startTime = time;

        // background (simple, unobtrusive)
        int x0 = 0, y0 = 0, x1 = width(), y1 = height();
        gg.fill(x0, y0, x1, y1, 0xCC101010);
        gg.fill(x0, y0, x1, y0 + 1, 0x44FFFFFF); // top line

        // icon + count overlay
        gg.renderItem(icon, 6, 8);
        gg.renderItemDecorations(Minecraft.getInstance().font, icon, 6, 8);

        // text
        gg.drawString(Minecraft.getInstance().font, title, 32, 7, 0xFFFFFF, false);
        gg.drawString(Minecraft.getInstance().font, desc, 32, 18, 0xC0C0C0, false);

        if (!playedSound && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7f, 1.0f);
            playedSound = true;
        }

        return (time - startTime) < DISPLAY_MS ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override public int width()  { return WIDTH; }
    @Override public int height() { return HEIGHT; }
}
