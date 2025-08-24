package com.holysweet.questshop.service;

import com.holysweet.questshop.api.coins.AccountRef;
import com.holysweet.questshop.api.coins.CoinsProvider;
import com.holysweet.questshop.network.Net;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class CoinsService {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final AtomicReference<CoinsProvider> ACTIVE =
            new AtomicReference<>(new VanillaCoinsProvider());

    private CoinsService() {}

    public static CoinsProvider provider() {
        return ACTIVE.get();
    }

    public static void swapBackend(CoinsProvider provider) {
        Objects.requireNonNull(provider, "provider");
        CoinsProvider prev = ACTIVE.getAndSet(provider);
        LOGGER.info("Coins provider swapped: '{}' -> '{}'",
                prev == null ? "<none>" : prev.id(), provider.id());
    }

    // -------- Player-oriented convenience (preferred) --------

    public static AccountRef handleFor(ServerPlayer player) {
        return ACTIVE.get().handleFor(player);
    }

    public static int get(ServerLevel level, ServerPlayer player) {
        return ACTIVE.get().get(level, handleFor(player));
    }

    public static int set(ServerLevel level, ServerPlayer player, int value) {
        AccountRef ref = handleFor(player);
        int v = ACTIVE.get().set(level, ref, value);
        broadcastChange(level, ref); // central sync
        return v;
    }

    public static int add(ServerLevel level, ServerPlayer player, int delta) {
        AccountRef ref = handleFor(player);
        int v = ACTIVE.get().add(level, ref, delta);
        broadcastChange(level, ref); // central sync
        return v;
    }

    // -------- Direct-handle operations (admin/advanced) --------

    public static int get(ServerLevel level, AccountRef handle) {
        return ACTIVE.get().get(level, handle);
    }

    public static int set(ServerLevel level, AccountRef handle, int value) {
        int v = ACTIVE.get().set(level, handle, value);
        broadcastChange(level, handle); // central sync
        return v;
    }

    public static int add(ServerLevel level, AccountRef handle, int delta) {
        int v = ACTIVE.get().add(level, handle, delta);
        broadcastChange(level, handle); // central sync
        return v;
    }

    // -------- Sync/broadcast (provider-agnostic) --------
    // For a Player account: sync that player.
    // For a Team account: sync all online players whose handle equals the changed handle.
    private static void broadcastChange(ServerLevel level, AccountRef changed) {
        MinecraftServer server = level.getServer();

        if (changed instanceof AccountRef.Player(UUID playerId)) {
            ServerPlayer sp = server.getPlayerList().getPlayer(playerId);
            if (sp != null) {
                Net.syncBalance(sp);
            }
            return;
        }

        if (changed instanceof AccountRef.Team teamRef) {
            for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                AccountRef current = ACTIVE.get().handleFor(sp);
                if (current.equals(teamRef)) {
                    Net.syncBalance(sp);
                }
            }
        }
    }
}
