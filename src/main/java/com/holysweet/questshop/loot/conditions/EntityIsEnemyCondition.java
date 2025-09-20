package com.holysweet.questshop.loot.conditions;

import com.holysweet.questshop.loot.LootConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

public class EntityIsEnemyCondition implements LootItemCondition {
    public static final EntityIsEnemyCondition INSTANCE = new EntityIsEnemyCondition();
    public static final MapCodec<EntityIsEnemyCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public @NotNull LootItemConditionType getType() {
        return LootConditions.ENTITY_IS_ENEMY.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        return entity instanceof Enemy;
    }

    public static LootItemCondition.Builder entityIsMonster() {
        return () -> INSTANCE;
    }
}
