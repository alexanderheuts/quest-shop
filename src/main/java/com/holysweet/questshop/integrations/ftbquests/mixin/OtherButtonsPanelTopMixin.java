package com.holysweet.questshop.integrations.ftbquests.mixin;

import com.holysweet.questshop.integrations.ftbquests.client.QuestShopScreenButton;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftbquests.client.gui.quests.OtherButtonsPanelTop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OtherButtonsPanelTop.class, remap = false)
public abstract class OtherButtonsPanelTopMixin extends Panel {

    protected OtherButtonsPanelTopMixin(Panel panel) {
        super(panel);
    }

    @Inject(method = "addWidgets", at = @At("TAIL"))
    private void questshop$addShopButton(CallbackInfo ci) {
        add(new QuestShopScreenButton(this));
    }
}