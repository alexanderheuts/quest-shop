package com.holysweet.questshop.service;

import com.holysweet.questshop.api.coins.CoinsProvider;
import com.holysweet.questshop.api.coins.AccountRef;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class CoinsService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicReference<CoinsProvider> ACTIVE = new AtomicReference<>(new VanillaCoinsProvider());

    private CoinsService() {}

    public static void swapBackend(CoinsProvider backend) {
        Objects.requireNonNull(backend, "backend");
        CoinsProvider prev = ACTIVE.getAndSet(backend);
        LOGGER.info("CoinsService backend swapped: '{}' -> '{}'",
                prev == null ? "<none>" : prev.id(), backend.id());
    }

    public static AccountRef handleFor(ServerPlayer player) {
        return ACTIVE.get().handleFor(player);
    }

    public static int get(ServerLevel level, ServerPlayer player) {
        return ACTIVE.get().get(level, handleFor(player));
    }

    public static int set(ServerLevel level, ServerPlayer player, int value) {
        return ACTIVE.get().set(level, handleFor(player), value);
    }

    public static int add(ServerLevel level, ServerPlayer player, int delta) {
        return ACTIVE.get().add(level, handleFor(player), delta);
    }

    public static int get(ServerLevel level, AccountRef handle) {
        return ACTIVE.get().get(level, handle);
    }

    public static int set(ServerLevel level, AccountRef handle, int value) {
        return ACTIVE.get().set(level, handle, value);
    }

    public static int add(ServerLevel level, AccountRef handle, int delta) {
        return ACTIVE.get().add(level, handle, delta);
    }
}
