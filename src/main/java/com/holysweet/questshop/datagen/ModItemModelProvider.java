package com.holysweet.questshop.datagen;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, QuestShop.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.COIN.get());
    }
}
