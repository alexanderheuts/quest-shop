package com.holysweet.questshop.service;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.coins.AccountRef;
import com.holysweet.questshop.api.coins.CoinsProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class VanillaCoinsProvider implements CoinsProvider {
    private static final String NBT_KEY = QuestShop.MODID + ":coins";

    public VanillaCoinsProvider() {}

    @Override
    public String id() {
        return "vanilla";
    }

    @Override
    public AccountRef handleFor(ServerPlayer player) {
        return new AccountRef.Player(player.getUUID());
    }

    @Override
    public int get(ServerLevel level, AccountRef handle) {
        ServerPlayer p = player(level.getServer(), ((AccountRef.Player) handle).playerId());
        return p == null ? 0 : p.getPersistentData().getInt(NBT_KEY);
    }

    @Override
    public int set(ServerLevel level, AccountRef handle, int value) {
        ServerPlayer p = player(level.getServer(), ((AccountRef.Player) handle).playerId());
        if (p == null) {
            return 0;
        }
        int v = Math.max(0, value);
        CompoundTag tag = p.getPersistentData();
        tag.putInt(NBT_KEY, v);
        return v;
    }

    private static ServerPlayer player(MinecraftServer server, UUID id) {
        return server.getPlayerList().getPlayer(id);
    }
}
