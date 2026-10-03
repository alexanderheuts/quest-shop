package com.holysweet.questshop.integrations.ftbquests.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class FTBQuestsMixinPlugin implements IMixinConfigPlugin {

    private static final String FTB_QUESTS_OTHERBUTTONSPANELTOP_CLASS =
            "dev/ftb/mods/ftbquests/client/gui/quests/OtherButtonsPanelTop.class";
    
    private boolean isFTBQuestsPresent;

    @Override
    public void onLoad(String mixinPackage) {
        this.isFTBQuestsPresent = getClass().getClassLoader().getResource(
                FTB_QUESTS_OTHERBUTTONSPANELTOP_CLASS                
        ) != null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("OtherButtonsPanelTopMixin")) {
            return this.isFTBQuestsPresent;
        }
        return true;
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}