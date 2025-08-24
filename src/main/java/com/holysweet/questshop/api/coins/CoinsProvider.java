package com.holysweet.questshop.api.coins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface CoinsProvider {
    String id();

    AccountRef handleFor(ServerPlayer player);

    int get(ServerLevel level, AccountRef handle);

    int set(ServerLevel level, AccountRef handle, int value);

    default int add(ServerLevel level, AccountRef handle, int delta) {
        return set(level, handle, get(level, handle) + delta);
    }
}
