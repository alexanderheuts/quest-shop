package com.holysweet.questshop.loot;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.loot.conditions.EntityIsEnemyCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class LootConditions {
    public static final DeferredRegister<LootItemConditionType> CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, QuestShop.MODID);

    public static final Supplier<LootItemConditionType> ENTITY_IS_ENEMY =
            CONDITIONS.register("entity_is_enemy", () -> new LootItemConditionType(EntityIsEnemyCondition.CODEC));
}
