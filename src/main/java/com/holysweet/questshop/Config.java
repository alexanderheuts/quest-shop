package com.holysweet.questshop;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOOT_COIN_FROM_ENEMY = BUILDER
            .comment("Enemies can drop coins when killed. (values: true, false)")
            .define("lootCoinFromEnemy", true);

    public static final ModConfigSpec.DoubleValue LOOT_COIN_DROP_CHANCE = BUILDER
            .comment("Coin drop chance from enemies. (values: 0 to 1, where 1 is 100% chance. Default: 0.05")
            .defineInRange("lootCoinDropChance", 0.05, 0.0, 1.0);

    static final ModConfigSpec SPEC = BUILDER.build();
}
