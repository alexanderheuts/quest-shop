package com.holysweet.questshop.integrations.ftbquests.client;

import com.holysweet.questshop.item.ModItems;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftbquests.client.gui.quests.TabButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class QuestShopScreenButton extends TabButton {

    public QuestShopScreenButton(Panel panel) {
        super(panel,
                Component.translatable("sidebar_button.questshop.shop"),
                ItemIcon.getItemIcon(ModItems.COIN.get()));
    }

    @Override
    public void onClicked(MouseButton button) {
        playClickSound();
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.connection.sendCommand("hqs shop");
        }
    }
}