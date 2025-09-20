package com.holysweet.questshop.datagen;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.item.ModItems;
import com.holysweet.questshop.loot.CoinLootModifier;
import com.holysweet.questshop.loot.conditions.EntityIsEnemyCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, QuestShop.MODID);
    }

    @Override
    protected void start() {
        this.add("coin_from_entities",
                new CoinLootModifier(new LootItemCondition[] {
                        EntityIsEnemyCondition.entityIsMonster().build()
                }, ModItems.COIN.get()));
    }
}
